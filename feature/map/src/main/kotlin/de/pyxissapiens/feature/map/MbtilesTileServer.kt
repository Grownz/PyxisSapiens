package de.pyxissapiens.feature.map

import java.io.BufferedOutputStream
import java.net.ServerSocket
import java.net.Socket

/**
 * Tiny local HTTP server that serves raster tiles from an MBTiles file, so MapLibre can render
 * offline basemaps via a RasterSource pointing at `http://127.0.0.1:<port>/{z}/{x}/{y}`.
 *
 * Only GET requests for `/<z>/<x>/<y>.<ext>` are handled; tiles are read directly from SQLite.
 */
class MbtilesTileServer(private val source: MbtilesSource) {

    private var serverSocket: ServerSocket? = null
    private var acceptThread: Thread? = null
    private val format: String = source.metadata()["format"] ?: "png"

    var port: Int = 0
        private set

    val urlTemplate: String get() = "http://127.0.0.1:$port/{z}/{x}/{y}"

    fun start() {
        if (serverSocket != null) return
        val socket = ServerSocket(0)
        serverSocket = socket
        port = socket.localPort
        acceptThread = Thread { acceptLoop(socket) }.apply { isDaemon = true; name = "mbtiles-tiles"; start() }
    }

    fun stop() {
        runCatching { serverSocket?.close() }
        serverSocket = null
        acceptThread?.interrupt()
        acceptThread = null
    }

    private fun acceptLoop(server: ServerSocket) {
        while (!server.isClosed) {
            val client = try { server.accept() } catch (_: Exception) { break }
            Thread { handle(client) }.apply { isDaemon = true; start() }
        }
    }

    private fun handle(client: Socket) {
        client.use { socket ->
            val reader = socket.getInputStream().bufferedReader()
            val requestLine = reader.readLine() ?: return
            // Skip headers
            while (true) {
                val line = reader.readLine() ?: break
                if (line.isEmpty()) break
            }
            val path = requestLine.split(' ').getOrNull(1) ?: return
            val tile = parseTile(path)
            val out = BufferedOutputStream(socket.getOutputStream())
            if (tile == null) {
                writeResponse(out, 404, "text/plain", "not found".toByteArray())
                return
            }
            val (z, x, y) = tile
            val bytes = source.tile(z, x, y)
            if (bytes == null) {
                writeResponse(out, 204, "text/plain", ByteArray(0))
            } else {
                writeResponse(out, 200, "image/$format", bytes)
            }
        }
    }

    private fun parseTile(path: String): Triple<Int, Int, Int>? {
        val parts = path.trimStart('/').split('/')
        if (parts.size < 3) return null
        val z = parts[0].toIntOrNull() ?: return null
        val x = parts[1].toIntOrNull() ?: return null
        val y = parts[2].substringBefore('.').toIntOrNull() ?: return null
        return Triple(z, x, y)
    }

    private fun writeResponse(out: BufferedOutputStream, code: Int, contentType: String, body: ByteArray) {
        val header = buildString {
            append("HTTP/1.1 $code\r\n")
            append("Content-Type: $contentType\r\n")
            append("Content-Length: ${body.size}\r\n")
            append("Access-Control-Allow-Origin: *\r\n")
            append("Connection: close\r\n\r\n")
        }
        out.write(header.toByteArray())
        if (body.isNotEmpty()) out.write(body)
        out.flush()
    }
}

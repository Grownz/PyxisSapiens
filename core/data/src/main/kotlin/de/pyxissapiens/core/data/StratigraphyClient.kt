package de.pyxissapiens.core.data

import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional Macrostrat lookup: fetches the geologic units near a coordinate.
 * Offline-safe: any network/parse failure yields an empty result.
 */
@Singleton
class StratigraphyClient @Inject constructor(
    @Suppress("unused") @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun unitsAt(latitude: Double, longitude: Double): List<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(
                "https://macrostrat.org/api/v2/geologic_units/map?lat=$latitude&lng=$longitude&format=json",
            )
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 6000
                requestMethod = "GET"
            }
            try {
                if (connection.responseCode !in 200..299) return@runCatching emptyList()
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parseUnits(body)
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(emptyList())
    }

    private fun parseUnits(body: String): List<String> {
        val root = json.parseToJsonElement(body).jsonObject
        val data = root["success"]?.jsonObject?.get("data")?.jsonArray ?: return emptyList()
        val names = LinkedHashSet<String>()
        for (element in data) {
            val obj = element.jsonObject
            val candidates = listOf("strat_name", "unit_name", "name", "lith")
            for (key in candidates) {
                obj[key]?.jsonPrimitive?.contentOrNullSafe()?.takeIf { it.isNotBlank() }?.let { names.add(it) }
            }
        }
        return names.toList().take(20)
    }

    private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
        runCatching { content }.getOrNull()
}

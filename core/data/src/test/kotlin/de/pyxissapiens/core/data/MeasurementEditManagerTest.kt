package de.pyxissapiens.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.domain.model.Attitude
import de.pyxissapiens.core.domain.model.GeoPoint
import de.pyxissapiens.core.domain.model.Linework
import de.pyxissapiens.core.domain.model.LineworkKind
import de.pyxissapiens.core.domain.model.Measurement
import de.pyxissapiens.core.domain.model.MeasurementKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MeasurementEditManagerTest {

    private lateinit var db: PyxisDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PyxisDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    private fun measurement(id: String = "m1") = Measurement(
        id = id, siteId = "field", kind = MeasurementKind.PLANE,
        attitude = Attitude(30.0, 120.0), createdAt = 1L, updatedAt = 1L,
    )

    @Test
    fun addUndoRedoDelete() = runBlocking {
        val manager = MeasurementEditManager(db)

        manager.add(measurement())
        assertEquals(1, db.measurementDao().getAllOnce().size)
        assertTrue(manager.canUndo.value)

        manager.undo()
        assertEquals(0, db.measurementDao().getAllOnce().size)
        assertTrue(manager.canRedo.value)

        manager.redo()
        assertEquals(1, db.measurementDao().getAllOnce().size)

        assertTrue(manager.delete("m1"))
        assertEquals(0, db.measurementDao().getAllOnce().size)

        manager.undo()
        assertEquals(1, db.measurementDao().getAllOnce().size)
    }

    @Test
    fun updateWritesHistory() = runBlocking {
        val manager = MeasurementEditManager(db)
        manager.add(measurement())
        manager.update(measurement().copy(attitude = Attitude(45.0, 120.0)))

        val history = db.measurementHistoryDao().historyOf("m1")
        assertTrue(history.any { it.field == "dip" })
    }

    @Test
    fun repositoryMappingRoundTrip() = runBlocking {
        val repository = MeasurementRepository(db)
        repository.add(
            Measurement(
                id = "m2", siteId = "field", kind = MeasurementKind.LINE,
                location = GeoPoint(47.0, 11.0, 1200.0, 3f), createdAt = 1L, updatedAt = 1L,
            ),
        )
        val loaded = repository.observeAll().first()
        assertEquals(1, loaded.size)
        assertEquals(47.0, loaded.first().location!!.latitude, 1e-9)
    }

    @Test
    fun lineworkGeoJsonRoundTrip() {
        val coordinates = listOf(GeoPoint(47.0, 11.0), GeoPoint(47.1, 11.1), GeoPoint(47.2, 11.0))
        val json = GeoJsonGeom.toGeoJson(LineworkKind.POLYGON, coordinates)
        val parsed = GeoJsonGeom.parse(json)
        assertEquals(3, parsed.size)
        assertEquals(47.0, parsed.first().latitude, 1e-9)
        assertEquals(11.1, parsed[1].longitude, 1e-9)
        assertFalse(json.contains("null"))
    }

    @Test
    fun lineworkRepositoryRoundTrip() = runBlocking {
        val repository = LineworkRepository(db)
        repository.upsert(
            Linework(
                id = "l1", projectId = "default", kind = LineworkKind.CONTACT,
                coordinates = listOf(GeoPoint(1.0, 2.0), GeoPoint(3.0, 4.0)),
                createdAt = 1L, updatedAt = 1L,
            ),
        )
        val loaded = repository.observeAll().first()
        assertEquals(1, loaded.size)
        assertEquals(2, loaded.first().coordinates.size)
    }
}

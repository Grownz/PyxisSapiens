package de.pyxissapiens.core.data

import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.database.entity.MeasurementEntity
import de.pyxissapiens.core.database.entity.MeasurementHistoryEntity
import de.pyxissapiens.core.domain.measure.MeasurementValidator
import de.pyxissapiens.core.domain.model.Measurement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies measurement edits and keeps an undo/redo stack plus a field-level audit history.
 * The stacks are in-memory; history rows are persisted for audit and the history view.
 */
@Singleton
class MeasurementEditManager @Inject constructor(private val db: PyxisDatabase) {

    private sealed interface Op {
        data class Upsert(val before: MeasurementEntity?, val after: MeasurementEntity) : Op
        data class Delete(val before: MeasurementEntity) : Op
    }

    private val undoStack = ArrayDeque<Op>()
    private val redoStack = ArrayDeque<Op>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    suspend fun add(measurement: Measurement): List<String> {
        val entity = measurement.toEntity()
        db.measurementDao().upsert(entity)
        appendHistory(entity.id, "created", null, measurement.kind.name)
        push(Op.Upsert(null, entity))
        return MeasurementValidator.validate(measurement)
    }

    suspend fun update(measurement: Measurement): List<String> {
        val before = db.measurementDao().getById(measurement.id)
        val entity = measurement.toEntity()
        db.measurementDao().upsert(entity)
        if (before != null) recordDiff(before, entity)
        push(Op.Upsert(before, entity))
        return MeasurementValidator.validate(measurement)
    }

    suspend fun delete(id: String): Boolean {
        val before = db.measurementDao().getById(id) ?: return false
        db.measurementDao().delete(id)
        appendHistory(id, "deleted", id, null)
        push(Op.Delete(before))
        return true
    }

    suspend fun undo(): Boolean {
        val op = undoStack.removeLastOrNull() ?: return false
        when (op) {
            is Op.Upsert -> if (op.before == null) db.measurementDao().delete(op.after.id) else db.measurementDao().upsert(op.before)
            is Op.Delete -> db.measurementDao().upsert(op.before)
        }
        redoStack.addLast(op)
        refreshFlags()
        return true
    }

    suspend fun redo(): Boolean {
        val op = redoStack.removeLastOrNull() ?: return false
        when (op) {
            is Op.Upsert -> db.measurementDao().upsert(op.after)
            is Op.Delete -> db.measurementDao().delete(op.before.id)
        }
        undoStack.addLast(op)
        refreshFlags()
        return true
    }

    private fun push(op: Op) {
        undoStack.addLast(op)
        if (undoStack.size > 100) undoStack.removeFirst()
        redoStack.clear()
        refreshFlags()
    }

    private fun refreshFlags() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }

    private suspend fun recordDiff(before: MeasurementEntity, after: MeasurementEntity) {
        val fields = mapOf(
            "kind" to (before.kind to after.kind),
            "dip" to (before.dip?.toString() to after.dip?.toString()),
            "dipDirection" to (before.dipDirection?.toString() to after.dipDirection?.toString()),
            "trend" to (before.trend?.toString() to after.trend?.toString()),
            "plunge" to (before.plunge?.toString() to after.plunge?.toString()),
            "bearing" to (before.bearing?.toString() to after.bearing?.toString()),
            "note" to (before.note to after.note),
            "qualityScore" to (before.qualityScore?.toString() to after.qualityScore?.toString()),
        )
        for ((field, pair) in fields) {
            if (pair.first != pair.second) appendHistory(after.id, field, pair.first, pair.second)
        }
    }

    private suspend fun appendHistory(id: String, field: String, oldValue: String?, newValue: String?) {
        db.measurementHistoryDao().append(
            MeasurementHistoryEntity(
                measurementId = id,
                field = field,
                oldValue = oldValue,
                newValue = newValue,
                atEpochMillis = System.currentTimeMillis(),
            ),
        )
    }
}

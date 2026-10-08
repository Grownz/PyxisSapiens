package de.pyxissapiens.core.domain.model

import kotlinx.serialization.Serializable

/** One audit-history entry for a measurement. */
@Serializable
data class HistoryEntry(
    val measurementId: String,
    val field: String,
    val oldValue: String?,
    val newValue: String?,
    val atEpochMillis: Long,
)

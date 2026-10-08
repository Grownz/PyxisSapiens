package de.pyxissapiens.core.domain.model

import kotlinx.serialization.Serializable

/** User-defined measurement data type (bedding, cleavage, fault, ...). */
@Serializable
data class DataType(
    val id: String,
    val name: String,
    val colorHex: String = "#FFB000",
    val symbol: String = "STRIKE_DIP",
)

/** Stratigraphic / rock unit (named `RockUnit` to avoid clashing with `kotlin.Unit`). */
@Serializable
data class RockUnit(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val code: String? = null,
)

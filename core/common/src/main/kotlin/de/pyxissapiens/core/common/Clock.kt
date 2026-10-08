package de.pyxissapiens.core.common

/**
 * Abstraction over the system clock. Kept Android-free so the domain layer stays portable
 * (KMP-ready). Implemented on the platform side.
 */
fun interface Clock {
    fun nowEpochMillis(): Long
}

package de.pyxissapiens.core.common

/** Domain-level result type used across module boundaries. */
sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

/** Structured, domain-facing error (no Android/throwable leakage at use-case boundaries). */
data class AppError(
    val code: String,
    val message: String,
    val cause: Throwable? = null,
)

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

fun <T> AppResult<T>.getOrNull(): T? = (this as? AppResult.Success)?.value

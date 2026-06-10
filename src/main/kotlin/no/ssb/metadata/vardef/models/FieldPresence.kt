package no.ssb.metadata.vardef.models

/**
 * Type safe implementation of tri-state semantics wherein a field can:
 *
 * - Not be present
 * - Be present with a value supplied
 * - Be present with a null value supplied
 */
sealed interface FieldPresence<out T> {
    data object Undefined : FieldPresence<Nothing>

    data class Present<T>(
        val value: T,
    ) : FieldPresence<T>
}

/**
 * Used for required fields where we don't allow the value to be set to null.
 */
fun <T> FieldPresence<T>.orElse(current: T): T =
    when (this) {
        is FieldPresence.Undefined -> current
        is FieldPresence.Present -> {
            when (value) {
                null -> current
                else -> value
            }
        }
    }

fun <T> FieldPresence<T>.definedValueOrNull(): T? =
    when (this) {
        FieldPresence.Undefined -> null
        is FieldPresence.Present -> value
    }

/**
 * Used for nullable fields where it should be possible to set the value to null.
 */
fun <T> FieldPresence<T?>.applyNullable(current: T?): T? =
    when (this) {
        FieldPresence.Undefined -> current
        is FieldPresence.Present -> value
    }

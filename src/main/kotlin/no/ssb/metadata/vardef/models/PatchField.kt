package no.ssb.metadata.vardef.models

sealed interface PatchField<out T> {
    data object Undefined : PatchField<Nothing>

    data class Present<T>(
        val value: T,
    ) : PatchField<T>
}

fun <T> PatchField<T>.orElse(current: T): T =
    when (this) {
        PatchField.Undefined -> current
        is PatchField.Present -> value
    }

fun <T> PatchField<T>.definedValueOrNull(): T? =
    when (this) {
        PatchField.Undefined -> null
        is PatchField.Present -> value
    }

fun <T> PatchField<T?>.applyNullable(current: T?): T? =
    when (this) {
        PatchField.Undefined -> current
        is PatchField.Present -> value
    }

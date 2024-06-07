package no.ssb.metadata.validators

import jakarta.validation.constraints.Pattern
import kotlin.reflect.KClass

const val VARDEF_ID_PATTERN = "^[a-zA-Z0-9-_]{8}$"

/**
 * Vardef ID validation constraint annotation class.
 *
 */
@Pattern(regexp = VARDEF_ID_PATTERN)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.VALUE_PARAMETER,
)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class VardefId(
    val message: String = "ID is not valid",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = [],
)

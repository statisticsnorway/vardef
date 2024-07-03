package no.ssb.metadata.vardef.validators

import jakarta.validation.constraints.Pattern
import no.ssb.metadata.vardef.constants.VARDEF_ID_PATTERN
import kotlin.reflect.KClass

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

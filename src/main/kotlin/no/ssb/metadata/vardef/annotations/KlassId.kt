package no.ssb.metadata.vardef.annotations

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * The annotated element must be a valid Klass code belonging to the classification or code list referenced by id.
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
    AnnotationTarget.LOCAL_VARIABLE,
)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class KlassId(
    val message: String = "Code {validatedValue} is not a valid classification id",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

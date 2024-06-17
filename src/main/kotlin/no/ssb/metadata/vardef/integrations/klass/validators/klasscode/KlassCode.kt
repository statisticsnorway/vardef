package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

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
@Constraint(validatedBy = [KlassCodeValidator::class])
annotation class KlassCode(
    val id: String,
    val message: String = "Invalid klass code ({validatedValue})",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

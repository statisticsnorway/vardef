package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import jakarta.validation.Constraint

/**
 * The annotated element must be a valid Klass code
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
annotation class KlassCodeUnitType(
    val message: String = "Invalid klass code for unit type ({validatedValue})",
)

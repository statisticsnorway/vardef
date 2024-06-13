package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import jakarta.validation.Constraint

/**
 * The annotated list element must only contain valid Klass codes
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
annotation class KlassCodes(
    val message: String = "Invalid klass codes ({validatedValue})",
)

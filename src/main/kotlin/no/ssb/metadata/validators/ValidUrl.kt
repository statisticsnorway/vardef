package no.ssb.metadata.validators

import jakarta.validation.Constraint

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class ValidUrl(
    val message: String = "Invalid URL format",
)

package no.ssb.metadata.validators

import jakarta.validation.Constraint


@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class ValidBoolean(
    val message: String = "Invalid boolean",
)


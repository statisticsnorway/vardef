package no.ssb.metadata.annotations

import jakarta.validation.Constraint


@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class ValidDate(
    val message: String = "Invalid date format",
)


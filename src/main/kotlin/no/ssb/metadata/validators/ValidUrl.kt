package no.ssb.metadata.validators

import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.URL_PATTERN

@Pattern(regexp = URL_PATTERN, message = "Invalid URL format")
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class ValidUrl(
    val message: String = "Invalid URL format",
)

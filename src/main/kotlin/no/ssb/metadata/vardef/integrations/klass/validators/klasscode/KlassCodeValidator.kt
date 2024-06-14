package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext


class KlassCodeValidator : ConstraintValidator<KlassCode, String> {

    private lateinit var id: String

    override fun initialize(constraintAnnotation: KlassCode) {
        this.id = constraintAnnotation.id
    }

    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<KlassCode>?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (!::id.isInitialized) {
            throw IllegalStateException("id has not been initialized")
        }
        return ValidKlassCode.isValidKlassCode(value, id)
    }
}
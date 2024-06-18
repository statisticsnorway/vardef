package no.ssb.metadata.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton

@Factory // <1>
class ValidUrlFactory() {
    @Singleton // <2>
    fun urlValidator(): ConstraintValidator<ValidUrl, String> {
        return ConstraintValidator { value, _, _ -> ValidUrlValidator.isValid(value) }
    }

    @Singleton // <2>
    fun dateValidator(): ConstraintValidator<ValidDate, String> {
        return ConstraintValidator { value, _, _ -> ValidDateValidator.isValid(value) }
    }

    @Singleton // <2>
    fun booleanValidator(): ConstraintValidator<ValidBoolean, String> {
        return ConstraintValidator { value, _, _ -> ValidBooleanValidator.isValid(value) }
    }
}

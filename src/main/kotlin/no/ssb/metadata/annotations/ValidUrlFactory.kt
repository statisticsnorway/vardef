package no.ssb.metadata.annotations

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
}



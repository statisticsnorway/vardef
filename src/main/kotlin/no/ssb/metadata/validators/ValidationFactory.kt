package no.ssb.metadata.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton

@Factory // <1>
class ValidationFactory() {
    @Singleton // <2>
    fun booleanValidator(): ConstraintValidator<ValidBoolean, String> {
        return ConstraintValidator { value, _, _ -> ValidBooleanValidator.isValid(value) }
    }

    @Singleton // <2>
    fun variableStatusValidator(): ConstraintValidator<ValidVariableStatus, String> {
        return ConstraintValidator { value, _, _ -> ValidVariableStatusValidator.isValid(value) }
    }
}

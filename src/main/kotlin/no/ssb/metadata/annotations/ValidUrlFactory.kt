package no.ssb.metadata.annotations

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.annotations.ValidUrlValidator.isValid

@Factory // <1>
class ValidUrlFactory() {

    @Singleton // <2>
    fun urlValidator(): ConstraintValidator<ValidUrl, String> {
        return ConstraintValidator { value, _, _ -> isValid(value) }
    }
}



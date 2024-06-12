package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCodeUtil.isValid

@Factory
class KlassValidationFactory {
    @Singleton
    fun klassCodeValidator(): ConstraintValidator<KlassCode, String> {
        return ConstraintValidator { value, _, _ -> isValid(value) }
    }
}

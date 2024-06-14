package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodeUtil
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassId
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassIdUtil

@Factory
class KlassValidationFactory {
    @Singleton
    fun klassIdValidator(): ConstraintValidator<KlassId, String> {
        return ConstraintValidator { value, _, _ -> KlassIdUtil.isValidId(value) }
    }

    @Singleton
    fun klassCodeValidator(): ConstraintValidator<KlassCode, String> {
        return ConstraintValidator { value, _, _ -> KlassCodeUtil.isValidCode(value) }
    }
}

package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.*
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassId
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.ValidKlassId

@Factory
class KlassValidationFactory {
    @Singleton
    fun klassIdValidator(): ConstraintValidator<KlassId, String> {
        return ConstraintValidator { value, _, _ -> ValidKlassId.isValidId(value) }
    }

    @Singleton
    fun klassCodeValidator(): ConstraintValidator<KlassCode, String> {
        return KlassCodeValidator()
        //return ConstraintValidator { value, id, _ -> ValidKlassCode.isValidKlassCode(value, id.toString()) }
    }

}

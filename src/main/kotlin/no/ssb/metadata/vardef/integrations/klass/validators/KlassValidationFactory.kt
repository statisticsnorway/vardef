package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodeSubjectField
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodeUnitType
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.ValidKlassCodeSubjectField
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.ValidKlassCodeUnitType
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassId
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.ValidKlassId

@Factory
class KlassValidationFactory {
    @Singleton
    fun klassIdValidator(): ConstraintValidator<KlassId, String> {
        return ConstraintValidator { value, _, _ -> ValidKlassId.isValidId(value) }
    }

    @Singleton
    fun klassCodeUnitTypeValidator(): ConstraintValidator<KlassCodeUnitType, String> {
        return ConstraintValidator { value, _, _ -> ValidKlassCodeUnitType.isValidUnitCode(value) }
    }

    @Singleton
    fun klassCodeSubjectFieldValidator(): ConstraintValidator<KlassCodeSubjectField, String> {
        return ConstraintValidator { value, _, _ -> ValidKlassCodeSubjectField.isValidSubjectCode(value) }
    }
}

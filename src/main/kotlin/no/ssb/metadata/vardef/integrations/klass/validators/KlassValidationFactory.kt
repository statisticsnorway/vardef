package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassId
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.ValidKlassId
import kotlin.jvm.optionals.getOrElse

@Factory
class KlassValidationFactory {
    @Inject
    private lateinit var klassService: KlassService

    @Singleton
    fun klassIdValidator(): ConstraintValidator<KlassId, String> {
        return ConstraintValidator { value, _, _ -> ValidKlassId.isValidId(value) }
    }

    @Singleton
    fun klassCodeValidator(): ConstraintValidator<KlassCode, String> {
        return ConstraintValidator {
                value,
                annotationMetadata,
                _,
            ->
            value in
                klassService.getCodesFor(
                    annotationMetadata.get("id", String::class.java).getOrElse {
                        throw IllegalStateException("no id supplied on annotation")
                    },
                )
        }
    }
}

package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.annotations.KlassCode
import no.ssb.metadata.vardef.annotations.KlassId
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import kotlin.jvm.optionals.getOrElse

@Factory
class KlassValidationFactory(
    private val klassService: KlassService,
) {
    @Singleton
    fun klassCodeValidator(): ConstraintValidator<KlassCode, String> =
        ConstraintValidator {
                value,
                annotationMetadata,
                _,
            ->
            value == null ||
                value in
                klassService.getCodesFor(
                    annotationMetadata["id", String::class.java].getOrElse {
                        throw IllegalStateException("no id supplied on annotation")
                    },
                )
        }

    @Singleton
    fun klassIdValidator(): ConstraintValidator<KlassId, String> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || klassService.doesClassificationExist(value)
        }
}

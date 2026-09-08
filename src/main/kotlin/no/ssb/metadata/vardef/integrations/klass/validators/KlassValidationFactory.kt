package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.annotations.KlassCode
import no.ssb.metadata.vardef.annotations.KlassCodeAtLevel
import no.ssb.metadata.vardef.annotations.KlassId
import no.ssb.metadata.vardef.annotations.MeasurementTypeKlassCode
import no.ssb.metadata.vardef.annotations.SubjectFieldsKlassCode
import no.ssb.metadata.vardef.annotations.UnitTypesKlassCode
import no.ssb.metadata.vardef.config.KlassConfiguration
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import kotlin.jvm.optionals.getOrElse

@Factory
class KlassValidationFactory(
    private val klassService: KlassService,
    private val klassConfiguration: KlassConfiguration,
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
    fun klassCodeAtLevelValidator(): ConstraintValidator<KlassCodeAtLevel, String> =
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
                    annotationMetadata["level", Int::class.java].getOrElse {
                        throw IllegalStateException("no level supplied on annotation")
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

    @Singleton
    fun unitTypesKlassCodeValidator(): ConstraintValidator<UnitTypesKlassCode, String> =
        ConstraintValidator {
            value,
            _,
            _,
            ->
            value == null ||
                value in klassService.getCodesFor(klassConfiguration.unitTypesId())
        }

    @Singleton
    fun subjectFieldsKlassCodeValidator(): ConstraintValidator<SubjectFieldsKlassCode, String> =
        ConstraintValidator {
            value,
            _,
            _,
            ->
            value == null ||
                value in klassService.getCodesFor(klassConfiguration.subjectFieldsId())
        }

    @Singleton
    fun measurementTypeKlassCodeValidator(): ConstraintValidator<MeasurementTypeKlassCode, String> =
        ConstraintValidator {
            value,
            _,
            _,
            ->
            value == null ||
                value in
                klassService.getCodesFor(
                    klassConfiguration.measurementTypeId(),
                    klassConfiguration.measurementTypeLevel(),
                )
        }
}

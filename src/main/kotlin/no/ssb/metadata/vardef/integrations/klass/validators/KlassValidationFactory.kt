package no.ssb.metadata.vardef.integrations.klass.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import kotlin.jvm.optionals.getOrElse

@Factory
class KlassValidationFactory(
    private val klassService: KlassService,
) {
    @Singleton
    @ExecuteOn(TaskExecutors.BLOCKING)
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
    @ExecuteOn(TaskExecutors.BLOCKING)
    fun klassIdValidator(): ConstraintValidator<KlassId, String> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || klassService.doesClassificationExist(value)
        }
}

package no.ssb.metadata.vardef.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.annotations.ValidDateOrder
import no.ssb.metadata.vardef.models.Draft
import no.ssb.metadata.vardef.models.UpdateDraft
import no.ssb.metadata.vardef.utils.ServiceUtils.Companion.isCorrectDateOrder

@Factory
class VardefValidationFactory {
    @Singleton
    fun draftDateOrderValidator(): ConstraintValidator<ValidDateOrder, Draft> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || isCorrectDateOrder(value.validFrom, value.validUntil)
        }

    @Singleton
    fun updateDraftDateOrderValidator(): ConstraintValidator<ValidDateOrder, UpdateDraft> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || isCorrectDateOrder(value.validFrom, value.validUntil)
        }
}

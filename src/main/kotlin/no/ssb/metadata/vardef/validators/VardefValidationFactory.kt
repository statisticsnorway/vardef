package no.ssb.metadata.vardef.validators

import io.micronaut.context.annotation.Factory
import io.micronaut.validation.validator.constraints.ConstraintValidator
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.annotations.NotEmptyLanguageStringType
import no.ssb.metadata.vardef.annotations.ValidDateOrder
import no.ssb.metadata.vardef.models.Draft
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.UpdateDraft
import no.ssb.metadata.vardef.services.VariableDefinitionService

@Factory
class VardefValidationFactory {
    @Inject
    private lateinit var variableDefinitionService: VariableDefinitionService

    @Singleton
    fun draftDateOrderValidator(): ConstraintValidator<ValidDateOrder, Draft> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || variableDefinitionService.isCorrectDateOrder(value.validFrom, value.validUntil)
        }

    @Singleton
    fun updateDraftDateOrderValidator(): ConstraintValidator<ValidDateOrder, UpdateDraft> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null || variableDefinitionService.isCorrectDateOrder(value.validFrom, value.validUntil)
        }

    @Singleton
    fun notEmptyLanguageStringType(): ConstraintValidator<NotEmptyLanguageStringType, LanguageStringType> =
        ConstraintValidator {
                value,
                _,
                _,
            ->
            value == null ||
                SupportedLanguages.entries.any { language ->
                    val languageValue = value.getValidLanguage(language)?.trim()
                    !languageValue.isNullOrEmpty()
                }
        }
}

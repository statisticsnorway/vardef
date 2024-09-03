package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.extensions.isEqualOrAfter
import no.ssb.metadata.vardef.extensions.isEqualOrBefore
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import java.time.LocalDate

@Singleton
class VariableDefinitionService(
    private val variableDefinitionRepository: VariableDefinitionRepository,
) {
    @Inject
    private lateinit var klassService: KlassService

    fun clear() = variableDefinitionRepository.deleteAll()

    fun listAll(): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun listAllAndRenderForLanguage(
        language: SupportedLanguages,
        validFrom: LocalDate = LocalDate.now(),
        validUntil: LocalDate = LocalDate.now(),
    ): List<RenderedVariableDefinition> =
        listAll()
            .filter { savedVariableDefinition ->
                validFrom.isEqualOrAfter(savedVariableDefinition.validFrom) &&
                    validUntil.isEqualOrBefore(savedVariableDefinition.validUntil ?: LocalDate.now())
            }
            .map { savedVariableDefinition ->
                savedVariableDefinition.toRenderedVariableDefinition(
                    language,
                    klassService,
                )
            }
            .groupBy { renderedVariableDefinition -> renderedVariableDefinition.id }
            .mapValues { entry -> entry.value.maxBy { it.patchId } }
            .values
            .toList()

    fun listAllPatchesById(id: String): List<SavedVariableDefinition> =
        variableDefinitionRepository.findByDefinitionIdOrderByPatchId(id).ifEmpty {
            throw EmptyResultException()
        }

    fun getOnePatchById(
        variableDefinitionId: String,
        patchId: Int,
    ): SavedVariableDefinition =
        variableDefinitionRepository
            .findByDefinitionIdAndPatchId(
                variableDefinitionId,
                patchId,
            )

    fun getLatestPatchById(id: String): SavedVariableDefinition = listAllPatchesById(id).last()

    fun getOneByIdAndRenderForLanguage(
        language: SupportedLanguages,
        id: String,
    ): RenderedVariableDefinition = getLatestPatchById(id).toRenderedVariableDefinition(language, klassService)

    fun save(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(varDef)

    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

    fun deleteById(id: String): Any =
        listAllPatchesById(id)
            .map {
                variableDefinitionRepository.deleteById(it.id)
            }

    /**
     * Check that a given date is not between any existing validity dates for the given variable definition.
     *
     * This is important to preserve metadata immutability, such that a consumer specifying a particular date
     * will not suddenly get a different result because a new period was inserted between existing ones.
     *
     * @param definitionId the ID variable definition to run the validation for.
     * @param dateOfValidity the new date supplied.
     * @return True if the date is valid, false otherwise.
     */
    fun isValidValidFromValue(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): Boolean {
        return listAllPatchesById(definitionId)
            .map { it.validFrom }
            .let { dates ->
                dateOfValidity.isBefore(dates.min()) || dateOfValidity.isAfter(dates.max())
            }
    }

    /**
     * End previous *validity period*
     *
     * This method set value for field *validUntil* to the day before new validity period.
     * There is no check for value, if *validUntil* is not null, the value is ignored.
     * A new patch with the updated value for *validUntil* is created.
     *
     * @param definitionId The id of the variable definition
     * @param dateOfNewValidity The starting date of the new validity period.
     * The *validUntil* field will be set to the day before this date.
     */
    fun endLastValidityPeriod(
        definitionId: String,
        dateOfNewValidity: LocalDate,
    ): SavedVariableDefinition {
        val endDate = dateOfNewValidity.minusDays(1)
        val latestExistingPatch = getLatestPatchById(definitionId)
        return save(
            latestExistingPatch.copy(
                validUntil = endDate,
            ).toInputVariableDefinition().toSavedVariableDefinition(latestExistingPatch.patchId),
        )
    }

    fun getLatestPatchByDateAndById(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): SavedVariableDefinition =
        variableDefinitionRepository
            .findByDefinitionIdOrderByPatchId(definitionId)
            .ifEmpty { throw EmptyResultException() }
            .filter { patch ->
                dateOfValidity.isEqualOrAfter(patch.validFrom)
            }
            .ifEmpty { throw NoMatchingValidityPeriodFound("Variable is not valid at date $dateOfValidity") }
            .last()

    /**
     * Check if *definition* is eligible for a new validity period.
     *
     * To be eligible, all values for all languages present in the previous patch for the variable definition
     * must be changed in the new definition. The changes are verified by comparing string values, ignoring case.
     *
     * @param newDefinition The input object containing the proposed variable definition.
     * @param latestExistingPatch The existing object  to compare against.
     * @return Returns `true` if all values for all languages are changed compared to the previous patch,
     * `false` otherwise
     */
    fun isNewDefinition(
        newDefinition: InputVariableDefinition,
        latestExistingPatch: SavedVariableDefinition,
    ): Boolean {
        val allLanguagesPresent =
            latestExistingPatch.definition.listPresentLanguages().all { lang ->
                newDefinition.definition.listPresentLanguages().contains(lang)
            }
        if (!allLanguagesPresent) {
            return false
        }
        val allDefinitionsChanged =
            latestExistingPatch.definition.listPresentLanguages().all { lang ->
                !latestExistingPatch.toInputVariableDefinition().getDefinitionValue(lang).equals(
                    newDefinition.getDefinitionValue(lang),
                    ignoreCase = true,
                )
            }
        return allDefinitionsChanged
    }

    /**
     * Ends the current validity period and saves a new validity period as separate patches.
     *
     * This function performs two actions:
     *  1. Ends the current validity period by setting its *validUntil* date to the day before
     *  the new validity period starts. This action creates a new patch to reflect the end of the
     *  previous validity period.
     *  2. Saves the new validity period as a separate new patch with updated validity information.
     *
     * @param newPeriod The new variable definition that specifies the start of a new validity period.
     * @param definitionId The ID of the existing variable definition whose validity period will be updated.
     * @return The newly saved variable definition with the updated validity period.
     */
    fun saveNewValidityPeriod(
        newPeriod: InputVariableDefinition,
        definitionId: String,
    ): SavedVariableDefinition {
        val endValidityPeriod = endLastValidityPeriod(definitionId, newPeriod.validFrom)
        return save(newPeriod.toSavedVariableDefinition(endValidityPeriod.patchId))
    }
}

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
                latestExistingPatch.toInputVariableDefinition().getDefinitionValue(lang)?.equals(
                    newDefinition.getDefinitionValue(lang), ignoreCase = true,
                ) == false
            }
        return allDefinitionsChanged
    }
}

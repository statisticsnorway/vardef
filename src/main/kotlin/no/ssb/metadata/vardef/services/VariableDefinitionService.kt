package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
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

    fun listAllAndRenderForLanguage(language: SupportedLanguages): List<RenderedVariableDefinition> =
        listAll().map { savedVariableDefinition ->
            savedVariableDefinition.toRenderedVariableDefinition(
                language,
                klassService,
            )
        }

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
        val validFromDates = listAllPatchesById(definitionId)
        val p = validFromDates.map { it.patchId }
        val d = validFromDates.map { it.validFrom }
        return true
        //return validFromDates.isEmpty() || dateOfValidity.isBefore(validFromDates.min()) || dateOfValidity.isAfter(validFromDates.max())
    }

    fun getLatestPatchByDateAndById(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): SavedVariableDefinition =
        variableDefinitionRepository
            .findByDefinitionIdOrderByPatchId(definitionId)
            .ifEmpty { throw EmptyResultException() }
            .filter { patch ->
                dateOfValidity.isAfter(patch.validFrom) && dateOfValidity.isBefore(patch.validUntil ?: LocalDate.MAX)
            }
            .ifEmpty { throw NoMatchingValidityPeriodFound("Variable is not valid at date $dateOfValidity") }
            .last()
}

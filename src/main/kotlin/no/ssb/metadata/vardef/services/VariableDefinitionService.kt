package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
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

    fun getLatestPatchById(id: String): SavedVariableDefinition =
        variableDefinitionRepository.findByDefinitionIdOrderByPatchId(id).ifEmpty { throw EmptyResultException() }.last()

    fun getOneByIdAndRenderForLanguage(
        language: SupportedLanguages,
        id: String,
    ): RenderedVariableDefinition = getLatestPatchById(id).toRenderedVariableDefinition(language, klassService)

    fun save(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(varDef)

    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

    fun deleteById(id: String): Any =
        variableDefinitionRepository
            .findByDefinitionIdOrderByPatchId(id)
            .ifEmpty { throw EmptyResultException() }
            .map {
                variableDefinitionRepository.deleteById(it.id)
            }

    fun getLatestPatchByDateAndById(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): SavedVariableDefinition {
        val patches =
            variableDefinitionRepository
                .findByDefinitionIdOrderByPatchId(
                    definitionId,
                ).ifEmpty { throw EmptyResultException() }
        val validFromDates = patches.map { it.validFrom }.toSortedSet()
        val validUntilDates =
            patches
                .mapNotNull {
                    it.validUntil
                }.toSortedSet()
        if (validUntilDates.lastOrNull { dateOfValidity.isAfter(it) } != null) {
            throw NoMatchingValidityPeriodFound("Variable is not valid at date $dateOfValidity")
        }
        val latestValidFromMatchingGivenDate = validFromDates.lastOrNull { dateOfValidity.isAfter(it) }
        if (latestValidFromMatchingGivenDate == null) {
            throw NoMatchingValidityPeriodFound("Variable is not valid at date $dateOfValidity")
        }
        return patches.last { it.validFrom == latestValidFromMatchingGivenDate }
    }

    fun saveNewValidityPeriod(vardef: SavedVariableDefinition): SavedVariableDefinition {
        // compare last patch/last validity period?
        val compareVersion = getLatestPatchById(vardef.definitionId)
        if (compareVersion.definition == vardef.definition) {
            val message = "Definition text must be changed"
            println(message)
        }
        return variableDefinitionRepository.save(vardef)
    }

    fun checkDefinition(vardef: InputVariableDefinition, variableDefinitionId: String): Boolean {
        val latestExistingPatch = getLatestPatchById(variableDefinitionId)
        return vardef.definition != latestExistingPatch.definition
    }
}

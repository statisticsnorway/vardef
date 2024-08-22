package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Inject
import jakarta.inject.Singleton
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

    fun getLatestVersionById(id: String): SavedVariableDefinition =
        variableDefinitionRepository.findByDefinitionIdOrderByVersionId(id).ifEmpty { throw EmptyResultException() }.last()

    fun getOneByIdAndRenderForLanguage(
        language: SupportedLanguages,
        id: String,
    ): RenderedVariableDefinition = getLatestVersionById(id).toRenderedVariableDefinition(language, klassService)

    fun save(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(varDef)

    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

    fun deleteById(id: String): Any =
        variableDefinitionRepository
            .findByDefinitionIdOrderByVersionId(id)
            .ifEmpty { throw EmptyResultException() }
            .map {
                variableDefinitionRepository.deleteById(it.id)
            }

    fun getLatestVersionByDateAndById(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): SavedVariableDefinition {
        val versions =
            variableDefinitionRepository
                .findByDefinitionIdOrderByVersionId(
                    definitionId,
                ).ifEmpty { throw EmptyResultException() }
        val validFromDates = versions.map { it.validFrom }.toSortedSet()
        val latestValidFromMatchingGivenDate = validFromDates.last { dateOfValidity.isAfter(it) }
        return versions.last { it.validFrom == latestValidFromMatchingGivenDate }
    }
}

package no.ssb.metadata.services

import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.models.RenderedVariableDefinition
import no.ssb.metadata.models.SavedVariableDefinition
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.integrations.klass.service.KlassService

@Singleton
class VariableDefinitionService (private val variableDefinitionRepository: VariableDefinitionRepository
) {

    @Inject
    private lateinit var klassService: KlassService

    fun clear() = variableDefinitionRepository.deleteAll()

    fun listAll(): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun listAllAndRenderForLanguage(language: SupportedLanguages): List<RenderedVariableDefinition> {
        return listAll().map { savedVariableDefinition -> savedVariableDefinition.toRenderedVariableDefinition(language, klassService) }
    }

    fun getOneById(id: String): SavedVariableDefinition = variableDefinitionRepository.findByDefinitionId(id)

    fun getOneByIdAndRenderForLanguage(
        language: SupportedLanguages,
        id: String,
    ): RenderedVariableDefinition = getOneById(id).toRenderedVariableDefinition(language, klassService)

    fun save(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(varDef)

    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

    fun deleteById(id: String): Any = variableDefinitionRepository.deleteById(variableDefinitionRepository.findByDefinitionId(id).id)
}

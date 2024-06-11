package no.ssb.metadata.services

import jakarta.inject.Singleton
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.SavedVariableDefinition
import no.ssb.metadata.models.RenderedVariableDefinition
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Singleton
class VariableDefinitionService(private val variableDefinitionRepository: VariableDefinitionRepository) {
    fun clear() = variableDefinitionRepository.deleteAll()

    fun listAll(): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findAll()
            .toList()


    fun listAllAndRenderForLanguage(language: SupportedLanguages): List<RenderedVariableDefinition> {
        return listAll().map { savedVariableDefinition -> savedVariableDefinition.toRenderedVariableDefinition(language) }
    }

    fun getOneByIdAndRenderForLanguage(
        language: SupportedLanguages,
        id: String,
    ): RenderedVariableDefinition = variableDefinitionRepository.findByDefinitionId(id).toRenderedVariableDefinition(language)

    fun save(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(varDef)
}

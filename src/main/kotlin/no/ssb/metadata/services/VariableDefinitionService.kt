package no.ssb.metadata.services

import jakarta.inject.Singleton
import no.ssb.metadata.models.InputVariableDefinition
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.SavedVariableDefinition
import no.ssb.metadata.models.RenderedVariableDefinition
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Singleton
class VariableDefinitionService(private val variableDefinitionRepository: VariableDefinitionRepository) {
    fun findAll(): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun findByLanguage(language: SupportedLanguages): List<RenderedVariableDefinition> {
        return findAll().map { dao -> dao.toRenderedVariableDefinition(language) }
    }

    fun save(varDef: InputVariableDefinition): SavedVariableDefinition {
        //requireNotNull(varDef.id) { "Something went wrong while saving variable, 'id' is missing" }
        return variableDefinitionRepository.save(varDef.toSavedVariableDefinition())
    }
}

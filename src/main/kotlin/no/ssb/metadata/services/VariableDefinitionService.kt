package no.ssb.metadata.services

import jakarta.inject.Singleton
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Singleton
class VariableDefinitionService(private val variableDefinitionRepository: VariableDefinitionRepository) {
    fun findAll(): List<VariableDefinitionDAO> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun findByLanguage(language: SupportedLanguages): List<VariableDefinitionDTO> {
        return findAll().map { dao -> dao.toDTO(language) }
    }

    fun save(varDef: VariableDefinitionDAO): VariableDefinitionDAO = variableDefinitionRepository.save(varDef)
}

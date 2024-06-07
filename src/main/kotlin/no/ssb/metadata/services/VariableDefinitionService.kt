package no.ssb.metadata.services

import jakarta.inject.Singleton
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Singleton
class VariableDefinitionService(private val variableDefinitionRepository: VariableDefinitionRepository) {
    fun clear() = variableDefinitionRepository.deleteAll()

    fun listAll(): List<VariableDefinitionDAO> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun listAllAndRenderForLanguage(language: SupportedLanguages): List<VariableDefinitionDTO> {
        return listAll().map { dao -> dao.toDTO(language) }
    }

    fun getOneByIdAndRenderForLanguage(
        language: SupportedLanguages,
        id: String,
    ): VariableDefinitionDTO = variableDefinitionRepository.findByDefinitionId(id).toDTO(language)

    fun save(varDef: VariableDefinitionDAO): VariableDefinitionDAO = variableDefinitionRepository.save(varDef)
}

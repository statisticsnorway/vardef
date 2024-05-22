package no.ssb.metadata.services

import jakarta.inject.Singleton
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Singleton
class VariableDefinitionService(private val variableDefinitionRepository: VariableDefinitionRepository) {
    fun findAll(): List<VariableDefinitionDAO> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun findByLanguage(language: String): List<VariableDefinitionDTO> {
        val variables = findAll()
        val result: MutableList<VariableDefinitionDTO> = mutableListOf()
        for (variable in variables) {
            val variableDefinitionDTO =
                VariableDefinitionDTO(
                    variable.getName(language),
                    variable.shortName,
                    variable.getDefinition(language),
                )
            result.add(variableDefinitionDTO)
        }
        return result
    }

    fun save(vardef: VariableDefinitionDAO): VariableDefinitionDAO = variableDefinitionRepository.save(vardef)
}

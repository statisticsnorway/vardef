package no.ssb.metadata.services

import jakarta.inject.Singleton
import no.ssb.metadata.exceptions.UnknownLanguageException
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

    fun findByLanguage(language: String): List<VariableDefinitionDTO> {
        if (!SupportedLanguages.entries.toString().contains(language)) {
            throw UnknownLanguageException(
                "Unknown language code $language. Valid values are ${SupportedLanguages.entries.map { it.toString() }}",
            )
        }

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

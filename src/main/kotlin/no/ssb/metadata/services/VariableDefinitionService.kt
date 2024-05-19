package no.ssb.metadata.services

import io.micronaut.serde.annotation.Serdeable
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

    fun findByLanguage(language: String): List<VariableDefinitionDTO> {
        val variables = findAll()
        val result: MutableList<VariableDefinitionDTO> = mutableListOf()
        for (variable in variables) {
            val variableDefinitionDTO = getName(variable, language)?.let {
                getDefinition(variable, language)?.let { it1 ->
                    VariableDefinitionDTO(
                        it,variable.shortName , it1
                    )
                }
            }
            if (variableDefinitionDTO != null) {
                result.add(variableDefinitionDTO)
            }
        }
        return result
    }

    fun getName(
        variableDefinitionDAO: VariableDefinitionDAO,
        language: String,
    ): Map<SupportedLanguages,String> ?{
        for ((k, v) in variableDefinitionDAO.name) {
            if (k.toString() == language) {
                println(v)
                return mapOf(k to v)
            }
        }
        return null
    }

    @Serdeable.Serializable
    private fun getDefinition(
        variableDefinitionDAO: VariableDefinitionDAO,
        language: String,
    ): Map<SupportedLanguages,String>? {
        for ((k, v) in variableDefinitionDAO.definition) {
            if (k.toString() == language) {
                println(v)
                return mapOf(k to v)
            }
        }
        return null
    }
}

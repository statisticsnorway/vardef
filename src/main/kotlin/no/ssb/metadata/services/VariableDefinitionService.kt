package no.ssb.metadata.services

import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import no.ssb.metadata.models.VariableDefinition
import no.ssb.metadata.repositories.VariableDefinitionRepository

@Singleton
class VariableDefinitionService(private val variableDefinitionRepository: VariableDefinitionRepository) {
    fun findAll(): List<VariableDefinition> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun findByLanguage(language: String): List<VariableDefinitionRequest> {
        val variables = findAll()
        println(variables)
        val result: MutableList<VariableDefinitionRequest> = mutableListOf()
        val variableDefinitionRequest = VariableDefinitionRequest("", "", "")
        for (variable in variables) {
            variableDefinitionRequest.shortName = variable.shortName
            variableDefinitionRequest.name = getName(variable, language)
            variableDefinitionRequest.definition = getDefinition(variable, language)
            result.add(variableDefinitionRequest)
        }
        return result
    }

    private fun getName(
        variableDefinition: VariableDefinition,
        language: String,
    ): String? {
        for ((k, v) in variableDefinition.name) {
            if (k.toString() == language) {
                println(v)
                return v
            }
        }
        return null
    }

    private fun getDefinition(
        variableDefinition: VariableDefinition,
        language: String,
    ): String? {
        for ((k, v) in variableDefinition.definition) {
            if (k.toString() == language) {
                println(v)
                return v
            }
        }
        return null
    }
}

@Serdeable
data class VariableDefinitionRequest(var name: String?, var shortName: String?, var definition: String?)

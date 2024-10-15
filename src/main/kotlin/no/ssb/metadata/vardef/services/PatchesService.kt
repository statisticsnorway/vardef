package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository

@Singleton
class PatchesService(
    private val variableDefinitionRepository: VariableDefinitionRepository,
) {
    fun list(definitionId: String): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findByDefinitionIdOrderByPatchId(definitionId)
            .ifEmpty { throw EmptyResultException() }

    fun get(
        definitionId: String,
        patchId: Int,
    ): SavedVariableDefinition =
        variableDefinitionRepository
            .findByDefinitionIdAndPatchId(
                definitionId,
                patchId,
            )

    fun latest(definitionId: String): SavedVariableDefinition =
        list(definitionId)
            .last()
}

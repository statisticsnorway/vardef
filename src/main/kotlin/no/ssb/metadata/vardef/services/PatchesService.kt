package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import kotlin.math.log

/**
 * Patches service
 *
 * Methods for working with Patches. All methods operate within one specific Variable Definition. Method return
 * types should be based on [SavedVariableDefinition].
 *
 * @property variableDefinitionRepository
 * @constructor Create empty Patches service
 */
@Singleton
class PatchesService(
    private val variableDefinitionRepository: VariableDefinitionRepository,
) {
    private val logger = LoggerFactory.getLogger(PatchesService::class.java)
    /**
     * Create a new *Patch*
     *
     * @param patch The *Patch* to create, with updated values.
     * @return The created *Patch*
     */
    fun create(patch: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(patch)

    /**
     * Private helper method for grouping patches by valid_from
     */
    private fun getAsMap(definitionId: String): SortedMap<LocalDate, List<SavedVariableDefinition>> =
        list(definitionId)
            .groupBy { it.validFrom }
            .toSortedMap()

    /**
     * Helper method
     */
    private fun listPeriods(definitionId: String): List<SavedVariableDefinition> =
        getAsMap(definitionId)
            .values
            .mapNotNull { it.maxByOrNull { patch -> patch.patchId } }
            .sortedBy { it.validFrom }

    /**
     * Create a new *Patch*
     *
     * If owner field is updated a new patch for each validity period is created
     *
     * @param patch The *Patch* to create, with updated values.
     * @return The created *Patch*
     */
    fun createPatch(
        patch: SavedVariableDefinition,
        validityPeriod: SavedVariableDefinition
    ): SavedVariableDefinition {
        val validityPeriods = listPeriods(patch.definitionId)
        if (patch.toPatch().owner != null) {
            validityPeriods
            //.filter { it.validFrom != validityPeriod }
            .forEach { period ->
                // Only save owner update for each validity period
                val patchWithOwner = period.copy(owner = patch.owner).toPatch()
                logger.info("Input is $patchWithOwner")
                logger.info("latest patch id is ${latest(patch.definitionId).patchId}")
                create(patchWithOwner.toSavedVariableDefinition(latest(patch.definitionId).patchId, period))
            }
            // Process and return the specified validityPeriod last
            // Save as normal patch
          // return create(patch)
        }
        // If no update on owner
        return create(patch.toPatch().toSavedVariableDefinition(latest(patch.definitionId).patchId, validityPeriod))
    }

    /**
     * List all Patches for a specific Variable Definition.
     *
     * The list is ordered by Patch ID.
     *
     * @param definitionId The ID of the Variable Definition.
     * @return An ordered list of all Patches for this Variable Definition.
     */
    fun list(definitionId: String): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findByDefinitionIdOrderByPatchId(definitionId)
            .ifEmpty { throw EmptyResultException() }

    /**
     * Get one Patch by ID.
     *
     * @param definitionId The ID of the Variable Definition.
     * @param patchId The ID of the Patch.
     * @return The specified Patch.
     */
    fun get(
        definitionId: String,
        patchId: Int,
    ): SavedVariableDefinition =
        variableDefinitionRepository
            .findByDefinitionIdAndPatchId(
                definitionId,
                patchId,
            )

    /**
     * Get the latest Patch for a specific Variable Definition.
     *
     * @param definitionId The ID of the Variable Definition.
     * @return The Patch with the highest Patch ID.
     */
    fun latest(definitionId: String): SavedVariableDefinition =
        list(definitionId)
            .last()

    /**
     * Delete all *Patches* in a *Variable Definition*
     *
     * @param definitionId The ID of the Variable Definition.
     */
    fun deleteAllForDefinitionId(definitionId: String) =
        list(definitionId)
            .forEach {
                variableDefinitionRepository.deleteById(it.id)
            }
}

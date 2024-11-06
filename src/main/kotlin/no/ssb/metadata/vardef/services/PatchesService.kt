package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import java.time.LocalDate
import java.util.*

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
    /**
     * Create a new *Patch*
     *
     * @param patch The *Patch* to create, with updated values.
     * @return The created *Patch*
     */
    fun create(patch: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(patch)

    /**
     * Private helper method for grouping patches by validity period.
     */
    private fun getAsMap(definitionId: String): SortedMap<LocalDate, List<SavedVariableDefinition>> =
        list(definitionId)
            .groupBy { it.validFrom }
            .toSortedMap()

    /**
     * Private helper method for listing validity periods with latest patch in each period.
     */
    private fun listPeriods(definitionId: String): List<SavedVariableDefinition> =
        getAsMap(definitionId)
            .values
            .mapNotNull { it.maxByOrNull { patch -> patch.patchId } }
            .sortedBy { it.validFrom }

    /**
     * Create new *Patch(es)*
     *
     * If owner field has new values a new patch for each validity period is created.
     * Only owner values for each validity period are saved, except from selected validity period where all
     * updated values are saved.
     *
     * If owner values are not changed one patch is created for selected validity period.
     *
     * @param patch The *Patch* to create, with updated values.
     * @param definitionId the id of the variable
     * @param latestPatch latest patch in selected validity period
     * @return The created *Patch* for selected validity period with all values updated
     */
    fun createPatch(
        patch: Patch,
        definitionId: String,
        latestPatch: SavedVariableDefinition,
    ): SavedVariableDefinition {
        val validityPeriods = listPeriods(definitionId)
        if (patch.owner != latestPatch.owner && patch.owner != null) {
            validityPeriods
                // The selected validity period must be handled separately
                .filter { it.validFrom != latestPatch.validFrom }
                .forEach { period ->
                    // We only want to update owner values in not selected validity periods
                    val patcOwner = patch.owner.let { period.copy(owner = it).toPatch() }
                    create(patcOwner.toSavedVariableDefinition(latest(definitionId).patchId, period))
                }
            // Process and handle the specified validity period, update all values
            return create(patch.toSavedVariableDefinition(latest(definitionId).patchId, latestPatch))
        }
        // If no update on owner
        return create(patch.toSavedVariableDefinition(latest(definitionId).patchId, latestPatch))
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

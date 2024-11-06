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
     * Group patches by *Validity Period*.
     */
    private fun getAsMap(definitionId: String): SortedMap<LocalDate, List<SavedVariableDefinition>> =
        list(definitionId)
            .groupBy { it.validFrom }
            .toSortedMap()

    /**
     * List of the latest *Patch* in each *Validity Period*.
     */
    private fun listPeriods(definitionId: String): List<SavedVariableDefinition> =
        getAsMap(definitionId)
            .values
            .mapNotNull { it.maxByOrNull { patch -> patch.patchId } }
            .sortedBy { it.validFrom }

    /**
     * Creates new *Patch* or *Patches*.
     *
     * This method generates patches according to changes in the owner field values across validity periods:
     * - If the owner field has new values, a separate patch is created for each validity period, containing only
     *   the owner values. For the selected validity period, however, all updated values
     *   are saved in the patch.
     * - If the owner values remain unchanged, a single patch is created for the selected validity period.
     *
     * @param patch The *Patch* containing updated values to apply.
     * @param definitionId The unique identifier for the variable.
     * @param latestPatch The latest existing patch within the selected validity period.
     * @return The created *Patch* for the selected validity period with all updated values applied.
     */
    fun createPatch(
        patch: Patch,
        definitionId: String,
        latestPatch: SavedVariableDefinition,
    ): SavedVariableDefinition {
        // Retrieve all validity periods associated with the given definition ID
        val validityPeriods = listPeriods(definitionId)

        // Check if the owner value has been updated and is not null
        if (patch.owner != latestPatch.owner && patch.owner != null) {
            validityPeriods
                // Exclude the currently selected validity period, which is handled separately
                .filter { it.validFrom != latestPatch.validFrom }
                .forEach { period ->
                    // For non-selected validity periods, only update the owner field
                    val patcOwner = patch.owner.let { period.copy(owner = it).toPatch() }
                    create(patcOwner.toSavedVariableDefinition(latest(definitionId).patchId, period))
                }
            // For the selected validity period, apply all updated values
            return create(patch.toSavedVariableDefinition(latest(definitionId).patchId, latestPatch))
        }
        // If no change in owner, create a patch for the selected validity period with the provided values
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

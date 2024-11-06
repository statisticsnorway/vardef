package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.security.VariableOwnerSecurityRule
import org.slf4j.LoggerFactory
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
     * Private helper method for listing patches
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
     * Only owner is updated in patches except for choosen validity period, there all
     * changes from patch is saved
     *
     * @param patch The *Patch* to create, with updated values.
     * @param latestPatch latest patch in selected validity period
     * @return The created *Patch*
     */
    fun createPatch(
        patch: Patch,
        definitionId: String,
        latestPatch: SavedVariableDefinition,
    ): SavedVariableDefinition {
        val validityPeriods = listPeriods(definitionId)
        logger.info("Checking is owner changed  ${patch.owner}")
        if (patch.owner != latestPatch.owner && patch.owner != null) {
            logger.info("Now owner can not be null  ${patch.owner}")
            var result: SavedVariableDefinition = latestPatch
            validityPeriods
                // the choosen validity period for the whole patch
                .filter { it.validFrom != latestPatch.validFrom }
                .forEach { period ->
                    // Only save owner update for each validity period
                    val patcOwner = patch.owner.let { period.copy(owner = it).toPatch() }
                    result = create(patcOwner.toSavedVariableDefinition(latest(definitionId).patchId, period))
                }
            // Process and return the specified validityPeriod last
            // Save as normal patch
            if (validityPeriods.any { it.validFrom == latestPatch.validFrom }) {
                logger.info("Still owner can not be null  ${patch.owner}")
                result = create(patch.toSavedVariableDefinition(latest(definitionId).patchId, latestPatch))
                logger.info("Saving in owner section ${result.owner}")
            }
            return result
        }
        // If no update on owner
        logger.info("Saving $patch")
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

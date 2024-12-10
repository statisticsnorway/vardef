package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.ssb.metadata.vardef.exceptions.InvalidOwnerStructureError
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import org.slf4j.LoggerFactory

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
    private val validityPeriodsService: ValidityPeriodsService,
) {
    private val logger = LoggerFactory.getLogger(PatchesService::class.java)

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
    fun create(
        patch: Patch,
        definitionId: String,
        latestPatch: SavedVariableDefinition,
    ): SavedVariableDefinition {
        if (patch.owner != latestPatch.owner && patch.owner != null) {
            if (!DaplaTeamService.containsDevelopersGroup(patch.owner)) {
                throw InvalidOwnerStructureError("Developers group of the owning team must be included in the groups list.")
            }
            validityPeriodsService
                .updateOwnerOnOtherPeriods(definitionId, patch.owner, latestPatch.validFrom)
        }
        // For the selected validity period create a patch with the provided values
        return variableDefinitionRepository.save(patch.toSavedVariableDefinition(latest(definitionId).patchId, latestPatch))
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
        list(definitionId).forEach { item ->
            variableDefinitionRepository.deleteById(item.id)
            logger.info(
                "Successfully deleted variable definition with id $definitionId",
                kv("definitionId", definitionId),
            )
        }
}

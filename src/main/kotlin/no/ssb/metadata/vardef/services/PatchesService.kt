package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.ssb.metadata.vardef.constants.DEFINITION_ID
import no.ssb.metadata.vardef.exceptions.ClosedValidityPeriodException
import no.ssb.metadata.vardef.exceptions.IllegalStatusChangeException
import no.ssb.metadata.vardef.exceptions.InvalidOwnerStructureError
import no.ssb.metadata.vardef.exceptions.InvalidValidDateException
import no.ssb.metadata.vardef.extensions.isEqualOrBefore
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.canTransitionTo
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

    @Inject
    lateinit var vardokIdMappingRepository: VardokIdMappingRepository

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
     *
     * @throws InvalidValidDateException if valid until date is equal or before valid from
     * @throws ClosedValidityPeriodException if attempt tp patch valid until on closed validity period
     */
    fun create(
        patch: Patch,
        definitionId: String,
        latestPatch: SavedVariableDefinition,
        userName: String,
    ): SavedVariableDefinition {
        if (patch.validUntil != null) {
            if (latestPatch.validUntil != null) {
                logger.error(
                    "Attempt to patch 'validUntil' on closed 'validityPeriod' for definition: $definitionId",
                    kv(DEFINITION_ID, definitionId),
                )
                throw ClosedValidityPeriodException()
            }
            if (latestPatch.validFrom.let { patch.validUntil.isEqualOrBefore(it) }) {
                logger.error(
                    "Invalid 'validUntil' value ${patch.validUntil} for definition: $definitionId",
                    kv(DEFINITION_ID, definitionId),
                )
                throw InvalidValidDateException()
            }
        }
        if (patch.owner != latestPatch.owner && patch.owner != null) {
            logger.info(
                "When creating patch owner has changed from ${latestPatch.owner} to ${patch.owner} for definition: $definitionId",
                kv(DEFINITION_ID, definitionId),
            )
            if (!DaplaTeamService.containsDevelopersGroup(patch.owner)) {
                logger.warn(
                    "Creating patch and ${patch.owner} not in developers-group for definition: $definitionId",
                    kv(DEFINITION_ID, definitionId),
                )
                throw InvalidOwnerStructureError("Developers group of the owning team must be included in the groups list.")
            }
            validityPeriodsService.updateOwnerOnOtherPeriods(definitionId, patch.owner, latestPatch.validFrom, userName)
            logger.info(
                "Creating patch and updating owner to ${patch.owner} on other periods for definition: $definitionId",
                kv(DEFINITION_ID, definitionId),
            )
        }

        if (patch.variableStatus != null) {
            if (!latestPatch.variableStatus.canTransitionTo(patch.variableStatus)) {
                throw IllegalStatusChangeException(
                    "Changing the status from ${latestPatch.variableStatus} to ${patch.variableStatus} is not allowed.",
                )
            }
            validityPeriodsService.updateStatusOnOtherPeriods(
                definitionId,
                patch.variableStatus,
                latestPatch.validFrom,
                userName,
            )
        }
        // For the selected validity period create a patch with the provided values
        val savedVariableDefinition =
            variableDefinitionRepository.save(
                patch.toSavedVariableDefinition(latest(definitionId).patchId, latestPatch, userName),
            )
        logger.info("Successfully saved patch for definition: $definitionId", kv(DEFINITION_ID, definitionId))
        logger.debug("New patch {}", savedVariableDefinition)
        return savedVariableDefinition
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
     * This includes deleting the patches and any associated Vardok vardef mappings.
     *
     * @param definitionId The ID of the Variable Definition.
     */
    fun deleteAllForDefinitionId(definitionId: String) {
        list(definitionId).forEach { item ->
            variableDefinitionRepository.deleteById(item.id)
        }
        if (vardokIdMappingRepository.existsByVardefId(definitionId)) {
            vardokIdMappingRepository.deleteByVardefId(definitionId)
            logger.info(
                "Vardok vardef mapping was deleted for definition: $definitionId", kv(DEFINITION_ID, definitionId))
        }
        logger.info("Successfully deleted all patches for definition: $definitionId", kv(DEFINITION_ID, definitionId))
    }
}

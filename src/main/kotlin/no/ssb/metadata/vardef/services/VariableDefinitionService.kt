package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import io.viascom.nanoid.NanoId
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.ssb.metadata.vardef.constants.DEFINITION_ID
import no.ssb.metadata.vardef.exceptions.InvalidOwnerStructureError
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate

/**
 * *Variable Definition* service.
 *
 * Functionality pertaining to entire *Variable Definitions*, where versioning is not directly visible.
 *
 * @property variableDefinitionRepository The [VariableDefinitionRepository] Bean to be injected
 * @property klassService The [KlassService] Bean to be injected
 * @property validityPeriods The [ValidityPeriodsService] Bean to be injected
 * @constructor Create empty Variable definition service
 */
@Singleton
class VariableDefinitionService(
    private val variableDefinitionRepository: VariableDefinitionRepository,
    private val klassService: KlassService,
    private val validityPeriods: ValidityPeriodsService,
) {
    private val logger = LoggerFactory.getLogger(VariableDefinitionService::class.java)

    /**
     * Create a new *Draft*
     *
     * @param draft The *Draft* to create.
     * @return The created *Draft*
     */
    fun create(draft: SavedVariableDefinition): SavedVariableDefinition {
        val savedVariableDefinition = variableDefinitionRepository.save(draft)
        logger.info(
            "Successful saved draft variable: ${savedVariableDefinition.shortName} for definition: $savedVariableDefinition.definitionId",
            kv(DEFINITION_ID, savedVariableDefinition.definitionId),
        )
        return savedVariableDefinition
    }

    /**
     * Update a Draft Variable Definition based on the provided [updateDraft].
     *
     * It checks if the owner has changed and validates the new owner by ensuring
     * the Developers group is included in the new owner's team groups.
     *
     * If the validation fails, an [InvalidOwnerStructureError] is thrown.
     *
     * @param savedDraft The current [SavedVariableDefinition] that is being updated.
     * @param updateDraft containing the new data to update the [SavedVariableDefinition].
     * @return The updated [SavedVariableDefinition]
     *
     * @throws InvalidOwnerStructureError
     *
     */
    fun update(
        savedDraft: SavedVariableDefinition,
        updateDraft: UpdateDraft,
    ): SavedVariableDefinition {
        updateDraft.owner.takeIf { it != savedDraft.owner }?.let {
            if (!DaplaTeamService.containsDevelopersGroup(it)) {
                throw InvalidOwnerStructureError("Developers group of the owning team must be included in the groups list.")
            }
        }
        val updatedVariable = variableDefinitionRepository.update(savedDraft.copyAndUpdate(updateDraft))
        logger.info(
            "Successful updated variable with id: ${updatedVariable.definitionId}",
            kv(DEFINITION_ID, updatedVariable.definitionId),
        )
        return updatedVariable
    }

    /**
     * List all objects in the repository
     */
    fun list(): List<SavedVariableDefinition> = variableDefinitionRepository.findAll()

    /**
     * Does the given short name already exist?
     *
     * @param shortName The value to check
     * @return `true` if the [shortName] exists, otherwise `false`
     */
    fun doesShortNameExist(shortName: String): Boolean {
        if (variableDefinitionRepository.existsByShortName(shortName)) {
            logger.info("Duplicate shortname $shortName")
            return true
        }
        return false
    }

    /**
     * @return `true` if [group] is an owner of the *Variable Definition*
     */
    fun groupIsOwner(
        group: String,
        definitionId: String,
    ) = group in validityPeriods.getLatestPatchInLastValidityPeriod(definitionId).owner.groups

    /**
     * Is the *Variable Definition* publicly available?
     *
     * @param definitionId The ID of the *Variable Definition* of interest.
     */
    fun isPublic(definitionId: String): Boolean = validityPeriods.getLatestPatchInLastValidityPeriod(definitionId).variableStatus.isPublic()

    private fun uniqueDefinitionIds(): Set<String> =
        variableDefinitionRepository
            .findDistinctDefinitionIdByVariableStatusInList(VariableStatus.entries.toList())

    private fun uniqueDefinitionIdsByStatus(variableStatus: VariableStatus): Set<String> =
        variableDefinitionRepository
            .findDistinctDefinitionIdByVariableStatusInList(listOf(variableStatus))

    /**
     * List *Variable Definitions* which are valid on the given date.
     *
     * If no date is given, list all variable definitions. The Variable Definitions
     * are rendered in the given language and are suitable for public use.
     *
     * @param language The language to render in.
     * @param dateOfValidity The date which *Variable Definitions* shall be valid at.
     * @return [List<RenderedVariableDefinition>] with status [VariableStatus.PUBLISHED_EXTERNAL] valid at the date.
     */
    fun listPublicForDate(
        language: SupportedLanguages,
        dateOfValidity: LocalDate?,
    ): List<RenderedVariableDefinition> {
        val results =
            uniqueDefinitionIdsByStatus(VariableStatus.PUBLISHED_EXTERNAL)
                .map {
                    getPublicByDate(language, it, dateOfValidity)
                }
        logger.info("Found ${results.size} valid public variable definitions at date $dateOfValidity.")
        return results
    }

    /**
     * List *Variable Definitions* which are valid on the given date.
     *
     * If no date is given, list all variable definitions. These are the
     * [CompleteResponse] and are suitable for internal use.
     *
     * @param dateOfValidity The date which *Variable Definitions* shall be valid at.
     * @return [List<CompleteResponse>] valid at the date.
     */
    fun listCompleteForDate(dateOfValidity: LocalDate?): List<CompleteResponse> {
        val results =
            uniqueDefinitionIds()
                .mapNotNull { getCompleteByDate(it, dateOfValidity) }
        logger.info("Found ${results.size} valid variable definitions at date $dateOfValidity.")
        return results
    }

    /**
     * One rendered *Variable Definition*, valid at the given date.
     *
     * - If [dateOfValidity] is `null`, get the latest valid version.
     * - If the variable is not valid on the given [dateOfValidity], return `null`
     *
     * @param language The language to render in.
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @param dateOfValidity The date which the *Variable Definition* shall be valid at.
     * @return The [RenderedVariableDefinition]
     * @throws [EmptyResultException] If nothing is found
     */
    fun getPublicByDate(
        language: SupportedLanguages,
        definitionId: String,
        dateOfValidity: LocalDate?,
    ): RenderedVariableDefinition =
        getByDateAndStatus(definitionId, dateOfValidity, VariableStatus.PUBLISHED_EXTERNAL)
            ?.render(language, klassService)
            ?: throw EmptyResultException()

    private fun getByDateAndStatus(
        definitionId: String,
        dateOfValidity: LocalDate? = null,
        variableStatus: VariableStatus? = null,
    ): SavedVariableDefinition? =
        getByDate(definitionId, dateOfValidity)
            .takeIf {
                variableStatus == null ||
                    it?.variableStatus == variableStatus
            }

    private fun getByDate(
        definitionId: String,
        dateOfValidity: LocalDate? = null,
    ): SavedVariableDefinition? =
        if (dateOfValidity == null) {
            validityPeriods.getLatestPatchInLastValidityPeriod(definitionId)
        } else {
            validityPeriods.getForDate(definitionId, dateOfValidity)
        }

    /**
     * Get a *Variable Definition*, valid on the given date.
     *
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @param dateOfValidity The date which the *Variable Definition* shall be valid at.
     * @return [CompleteResponse] suitable for internal use.
     */
    fun getCompleteByDate(
        definitionId: String,
        dateOfValidity: LocalDate? = null,
        variableStatus: VariableStatus? = null,
    ): CompleteResponse? = getByDateAndStatus(definitionId, dateOfValidity, variableStatus)?.toCompleteResponse()

    companion object {
        fun generateId(): String = NanoId.generate(8)
    }
}

package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import jakarta.inject.Singleton
import net.logstash.logback.argument.StructuredArguments.kv
import no.ssb.metadata.vardef.constants.DEFINITION_ID
import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.extensions.isEqualOrAfter
import no.ssb.metadata.vardef.extensions.isEqualOrBefore
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*

/**
 * *Validity Periods* service
 *
 * Methods relating to versioning *Variable Definitions* by date
 *
 * @property variableDefinitionRepository
 * @property klassService The [KlassService] Bean to be injected
 * @constructor Create empty Validity periods service
 */
@Singleton
class ValidityPeriodsService(
    private val klassService: KlassService,
    private val variableDefinitionRepository: VariableDefinitionRepository,
) {
    private val logger = LoggerFactory.getLogger(ValidityPeriodsService::class.java)

    /**
     * List all Patches for a specific Variable Definition.
     *
     * The list is ordered by Patch ID.
     *
     * @param definitionId The ID of the Variable Definition.
     * @return An ordered list of all Patches for this Variable Definition.
     */
    private fun list(definitionId: String): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findByDefinitionIdOrderByPatchId(definitionId)
            .ifEmpty { throw EmptyResultException() }

    /**
     * List latest Patches ordered by Validity Period.
     *
     * @param definitionId The ID of the Variable Definition.
     * @return An ordered list.
     */
    fun listLatestByValidityPeriod(definitionId: String): List<SavedVariableDefinition> =
        getAsMap(definitionId)
            .values
            .mapNotNull { it.maxByOrNull { patch -> patch.patchId } }
            .sortedBy { it.validFrom }

    /**
     * List rendered *Validity Periods*.
     *
     * A list of the latest *Patch* in each *Validity Period*. These are rendered and
     * suitable for display in public clients.
     *
     * @param language The language in which to render.
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @return The list of rendered *Validity Periods*
     */
    fun listPublic(
        language: SupportedLanguages,
        definitionId: String,
    ): List<RenderedVariableDefinition> =
        listLatestByValidityPeriod(definitionId)
            .map { it.render(language, klassService) }

    /**
     * List complete *Validity Periods*.
     *
     * A list of the latest *Patch* in each *Validity Period*. These are the [CompleteResponse] and suitable
     * for internal use.
     *
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @return The list of *Validity Periods*
     */
    fun listComplete(definitionId: String): List<CompleteResponse> =
        listLatestByValidityPeriod(definitionId)
            .map { it.toCompleteResponse() }

    /**
     * Get a map over *Validity Periods*.
     *
     * Map keys: The [SavedVariableDefinition.validFrom] date which defines the start of the *Validity Period*
     * Map values: All the *Patches* within that *Validity Period*, sorted by [SavedVariableDefinition.patchId]
     *
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @return The map over *Validity Periods*
     */
    fun getAsMap(definitionId: String): SortedMap<LocalDate, List<SavedVariableDefinition>> =
        list(definitionId)
            .groupBy { it.validFrom }
            .toSortedMap()

    /**
     * Get latest *Patch* in last *Validity Period*
     *
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @return The *Patch*
     */
    fun getLatestPatchInLastValidityPeriod(definitionId: String): SavedVariableDefinition =
        getAsMap(definitionId)
            .lastEntry()
            .value
            .last()

    /**
     * Get the *Validity Period* which is valid on the given date.
     *
     * @param definitionId The ID of the *Variable Definition* of interest.
     * @param dateOfValidity The date at which we are interested in the definition.
     * @return The latest *Patch* for the *Validity Period* valid at the given [dateOfValidity]
     */
    fun getForDate(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): SavedVariableDefinition? =
        getAsMap(definitionId)
            .filter { dateOfValidity.isEqualOrAfter(it.key) }
            .filter { dateOfValidity.isEqualOrBefore(it.value.last().validUntil ?: dateOfValidity) }
            .ifEmpty { return null }
            // Latest Validity Period starting before the given date
            .entries
            .last()
            // Latest patch in that Validity Period
            .value
            .last()

    /**
     * Get latest patch for validity period.
     *
     * Since Validity Periods must have a validFrom, we use this as an identifier.
     * - If the Valid From date is specified, we get the latest patch for the Matching Validity Period
     * - If the Valid From date is null, we get the latest patch for the most recent Validity Period
     *
     * @param definitionId Variable Definition ID
     * @param validFrom The Valid From date for the desired validity Period
     * @return the latest Patch
     */
    fun getMatchingOrLatest(
        definitionId: String,
        validFrom: LocalDate?,
    ): SavedVariableDefinition =
        if (validFrom == null) {
            getLatestPatchInLastValidityPeriod(definitionId)
        } else {
            getAsMap(definitionId)[validFrom]?.last()
                ?: throw NoMatchingValidityPeriodFound("No Validity Period with valid_from date $validFrom")
        }

    /**
     * Ends the current validity period and saves a new validity period as separate patches.
     *
     * If new valid from is before first validity period, new version valid until is set to the day
     * before first valid from. And only one new patch is created.
     *
     * Otherwise, two patches are created:
     *  1.Ends the current validity period by setting its *validUntil* date to the day before
     *  the new validity period starts. This action creates a new patch to reflect the end of the
     *  previous validity period.
     *  2. Saves the new validity period as a separate new patch with updated validity information.
     *
     * @param newPeriod The new variable definition that specifies the start of a new validity period.
     * @param definitionId The ID of the existing variable definition whose validity period will be updated.
     * @return The newly saved variable definition with the updated validity period.
     */
    fun create(
        definitionId: String,
        newPeriod: ValidityPeriod,
        userName: String,
    ): SavedVariableDefinition {
        val validityPeriodsMap = getAsMap(definitionId)

        checkValidityPeriodInput(definitionId, newPeriod)

        // Newest patch in the earliest Validity Period
        val firstValidityPeriod = validityPeriodsMap.firstEntry().value.last()
        // Newest patch in the latest Validity Period
        val lastValidityPeriod = validityPeriodsMap.lastEntry().value.last()
        logger.info(
            "Creating new validity period that is valid from ${newPeriod.validFrom} for definition: $definitionId",
            kv(DEFINITION_ID, definitionId),
        )
        return if (newPeriod.validFrom.isBefore(firstValidityPeriod.validFrom)) {
            newPeriod
                // A Validity Period to be created before all others uses the last one as base.
                // We know this has the most recent ownership and other info.
                // The user can Patch any values after creation.
                .toSavedVariableDefinition(list(definitionId).last().patchId, lastValidityPeriod, userName)
                .apply { validUntil = firstValidityPeriod.validFrom.minusDays(1) }
                .let { variableDefinitionRepository.save(it) }
        } else {
            endLastValidityPeriod(definitionId, newPeriod.validFrom, userName)
                .let { newPeriod.toSavedVariableDefinition(list(definitionId).last().patchId, it, userName) }
                // New validity period is always open-ended. A valid_until date may be set via a patch.
                .apply { validUntil = null }
                .let { variableDefinitionRepository.save(it) }
        }
    }

    /**
     * Check mandatory input for creating a new validity period
     * @param newPeriod The input data to check
     * @param definitionId The id for the variable definition to check
     * @throws InvalidValidFromException validFrom is invalid
     * @throws DefinitionTextUnchangedException definition text in all present languages has not changed
     */
    private fun checkValidityPeriodInput(
        definitionId: String,
        newPeriod: ValidityPeriod,
    ) {
        when {
            !isValidValidFromValue(definitionId, newPeriod.validFrom) -> {
                logger.error(
                    "Invalid 'validFrom' value ${newPeriod.validFrom} for definition: $definitionId",
                    kv(DEFINITION_ID, definitionId),
                )
                throw InvalidValidFromException()
            }

            !isNewDefinition(definitionId, newPeriod) -> {
                logger.error(
                    "Definition not changed ${newPeriod.validFrom} for definition: $definitionId",
                    kv(DEFINITION_ID, definitionId),
                )
                throw DefinitionTextUnchangedException()
            }
            else -> {
                logger.info(
                    "Validity period input is valid for definition: $definitionId",
                    kv(DEFINITION_ID, definitionId),
                )
            }
        }
    }

    /**
     * Check that a given date is not between any existing validity dates for the given variable definition.
     *
     * This is important to preserve metadata immutability, such that a consumer specifying a particular date
     * will not suddenly get a different result because a new period was inserted between existing ones.
     *
     * @param definitionId the ID variable definition to run the validation for.
     * @param dateOfValidity the new date supplied.
     * @return True if the date is valid, false otherwise.
     */
    private fun isValidValidFromValue(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): Boolean {
        // patches
        val patches = list(definitionId)

        // Extract validFrom and validUntil values
        val validFromDates = patches.map { it.validFrom }
        val validUntilDates = patches.map { it.validUntil }

        // Handling new validity period when there is only one that is closed on same patch
        if (validFromDates.size == 1 && validUntilDates.firstOrNull() != null) {
            val validFrom = validFromDates.first()
            val validUntil = validUntilDates.first()
            logger.info(
                "Checking if valid new valid from: $dateOfValidity is between validFrom: $validFrom and " +
                    "validUntil: $validUntil for definition: $definitionId",
                kv(DEFINITION_ID, definitionId),
            )
            return !(dateOfValidity.isAfter(validFrom) && dateOfValidity.isBefore(validUntil))
        }
        logger.info(
            "Checking if valid new valid from: $dateOfValidity is before: ${validFromDates.min()} " +
                "or after: ${validFromDates.max()} for definition: $definitionId",
            kv(DEFINITION_ID, definitionId),
        )
        return dateOfValidity.isBefore(validFromDates.minOrNull()) || dateOfValidity.isAfter(validFromDates.maxOrNull())
    }

    /**
     * Check if *definition* is eligible for a new validity period.
     *
     * To be eligible, all values for all languages present in the previous patch for the variable definition
     * must be changed in the new definition. The changes are verified by comparing string values, ignoring case.
     *
     * @param definitionId The ID of the Variable Definition to check
     * @param newPeriod The input object containing the proposed variable definition.
     * @return Returns `true` if all values for all languages are changed compared to the previous patch,
     * `false` otherwise
     */
    private fun isNewDefinition(
        definitionId: String,
        newPeriod: ValidityPeriod,
    ): Boolean {
        val lastValidityPeriod = getLatestPatchInLastValidityPeriod(definitionId)
        val allLanguagesPresent =
            lastValidityPeriod.definition.listPresentLanguages().all { lang ->
                newPeriod.definition.listPresentLanguages().contains(lang)
            }
        if (!allLanguagesPresent) {
            return false
        }
        val allDefinitionsChanged =
            lastValidityPeriod.definition.listPresentLanguages().all { lang ->
                val oldValue = lastValidityPeriod.definition.getValidLanguage(lang)
                val newValue = newPeriod.definition.getValidLanguage(lang)
                val changed = !oldValue.equals(newValue, ignoreCase = true)
                if (!changed) {
                    logger.warn(
                        "No change detected for language '$lang' and text: $newValue for definition: $definitionId",
                        kv(DEFINITION_ID, definitionId),
                    )
                }
                changed
            }

        return allDefinitionsChanged
    }

    /**
     * End previous *Validity Period*
     *
     * This method set value for field *validUntil* to the day before new validity period.
     * There is no check for value, if *validUntil* is not null, the value is ignored.
     * A new patch with the updated value for *validUntil* is created.
     *
     * @param definitionId The id of the variable definition
     * @param newPeriodValidFrom The starting date of the new *Validity Period*.
     *
     */
    fun endLastValidityPeriod(
        definitionId: String,
        newPeriodValidFrom: LocalDate,
        userName: String,
    ): SavedVariableDefinition {
        val latestPatchInLastValidityPeriod = getLatestPatchInLastValidityPeriod(definitionId)
        return variableDefinitionRepository.save(
            latestPatchInLastValidityPeriod
                .copy(validUntil = newPeriodValidFrom.minusDays(1))
                .toPatch()
                .toSavedVariableDefinition(list(definitionId).last().patchId, latestPatchInLastValidityPeriod, userName),
        )
    }

    fun updateOwnerOnOtherPeriods(
        definitionId: String,
        owner: Owner,
        validFrom: LocalDate,
        userName: String,
    ): Unit =
        listLatestByValidityPeriod(definitionId)
            // Exclude the matching validity period, which is handled separately
            .filter { it.validFrom != validFrom }
            .forEach { period ->
                // For non-selected validity periods, only update the owner field
                logger.info(
                    "Updating owner to $owner for definition: $definitionId " +
                        "for period between: ${period.validFrom} - ${ period.validUntil} ",
                    kv(DEFINITION_ID, definitionId),
                )
                val patchOwner = period.copy(owner = owner).toPatch()
                variableDefinitionRepository.save(
                    patchOwner.toSavedVariableDefinition(
                        list(definitionId).last().patchId,
                        period,
                        userName,
                    ),
                )
            }
}

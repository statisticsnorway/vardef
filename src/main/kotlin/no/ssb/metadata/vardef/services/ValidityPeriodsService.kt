package no.ssb.metadata.vardef.services

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.extensions.isEqualOrAfter
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.ValidityPeriod
import java.time.LocalDate
import java.util.*

@Singleton
class ValidityPeriodsService(
    private val patches: PatchesService,
    private val klassService: KlassService,
) {
    fun listValidityPeriodsById(
        language: SupportedLanguages,
        id: String,
    ): List<RenderedVariableDefinition> =
        listAllPatchesGroupedByValidityPeriods(id)
            .values
            .mapNotNull { it.maxByOrNull { patch -> patch.patchId } }
            .map { it.render(language, klassService) }
            .sortedBy { it.validFrom }

    fun listAllPatchesGroupedByValidityPeriods(definitionId: String): SortedMap<LocalDate, List<SavedVariableDefinition>> =
        patches
            .list(definitionId)
            .groupBy { it.validFrom }
            .toSortedMap()

    fun getLatestPatchInLastValidityPeriod(definitionId: String): SavedVariableDefinition =
        listAllPatchesGroupedByValidityPeriods(definitionId)
            .lastEntry()
            .value
            .last()

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
    fun isValidValidFromValue(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): Boolean =
        patches
            .list(definitionId)
            .map { it.validFrom }
            .let { dates ->
                dateOfValidity.isBefore(dates.min()) || dateOfValidity.isAfter(dates.max())
            }

    /**
     * End previous *validity period*
     *
     * This method set value for field *validUntil* to the day before new validity period.
     * There is no check for value, if *validUntil* is not null, the value is ignored.
     * A new patch with the updated value for *validUntil* is created.
     *
     * @param definitionId The id of the variable definition
     * @param newPeriodValidFrom The starting date of the new validity period.
     *
     */
    fun endLastValidityPeriod(
        definitionId: String,
        newPeriodValidFrom: LocalDate,
    ): SavedVariableDefinition {
        val latestPatchInLastValidityPeriod = getLatestPatchInLastValidityPeriod(definitionId)
        return patches.save(
            latestPatchInLastValidityPeriod
                .copy(
                    validUntil = newPeriodValidFrom.minusDays(1),
                ).toPatch()
                .toSavedVariableDefinition(patches.latest(definitionId).patchId, latestPatchInLastValidityPeriod),
        )
    }

    fun getLatestPatchByDateAndById(
        definitionId: String,
        dateOfValidity: LocalDate,
    ): SavedVariableDefinition =
        listAllPatchesGroupedByValidityPeriods(definitionId)
            .filter {
                dateOfValidity.isEqualOrAfter(it.key)
            }.ifEmpty { throw NoMatchingValidityPeriodFound("Variable is not valid at date $dateOfValidity") }
            // Latest Validity Period starting before the given date
            .entries
            .last()
            // Latest patch in that Validity Period
            .value
            .last()

    /**
     * Check if *definition* is eligible for a new validity period.
     *
     * To be eligible, all values for all languages present in the previous patch for the variable definition
     * must be changed in the new definition. The changes are verified by comparing string values, ignoring case.
     *
     * @param definitionId The ID of the Variable Definition to check
     * @param newDefinition The input object containing the proposed variable definition.
     * @return Returns `true` if all values for all languages are changed compared to the previous patch,
     * `false` otherwise
     */
    fun isNewDefinition(
        definitionId: String,
        newDefinition: ValidityPeriod,
    ): Boolean {
        val lastValidityPeriod = getLatestPatchInLastValidityPeriod(definitionId)
        val allLanguagesPresent =
            lastValidityPeriod.definition.listPresentLanguages().all { lang ->
                newDefinition.definition.listPresentLanguages().contains(lang)
            }
        if (!allLanguagesPresent) {
            return false
        }
        val allDefinitionsChanged =
            lastValidityPeriod.definition.listPresentLanguages().all { lang ->
                !lastValidityPeriod.toDraft().definition.getValidLanguage(lang).equals(
                    newDefinition.definition.getValidLanguage(lang),
                    ignoreCase = true,
                )
            }
        return allDefinitionsChanged
    }

    /**
     * Check mandatory input for creating a new validity period
     * @param newPeriod The input data to check
     * @param definitionId The id for the variable definition to check
     * @throws InvalidValidFromException validFrom is invalid
     * @throws DefinitionTextUnchangedException definition text in all present languages has not changed
     */
    private fun checkValidityPeriodInput(
        newPeriod: ValidityPeriod,
        definitionId: String,
    ) {
        when {
            !isValidValidFromValue(definitionId, newPeriod.validFrom) ->
                throw InvalidValidFromException()

            !isNewDefinition(definitionId, newPeriod) ->
                throw DefinitionTextUnchangedException()
        }
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
    fun saveNewValidityPeriod(
        newPeriod: ValidityPeriod,
        definitionId: String,
    ): SavedVariableDefinition {
        val validityPeriodsMap = listAllPatchesGroupedByValidityPeriods(definitionId)

        checkValidityPeriodInput(newPeriod, definitionId)

        // Newest patch in the earliest Validity Period
        val firstValidityPeriod = validityPeriodsMap.firstEntry().value.last()
        // Newest patch in the latest Validity Period
        val lastValidityPeriod = validityPeriodsMap.lastEntry().value.last()

        return if (newPeriod.validFrom.isBefore(firstValidityPeriod.validFrom)) {
            newPeriod
                // A Validity Period to be created before all others uses the last one as base.
                // We know this has the most recent ownership and other info.
                // The user can Patch any values after creation.
                .toSavedVariableDefinition(patches.latest(definitionId).patchId, lastValidityPeriod)
                .apply { validUntil = firstValidityPeriod.validFrom.minusDays(1) }
                .let { patches.save(it) }
        } else {
            endLastValidityPeriod(definitionId, newPeriod.validFrom)
                .let { newPeriod.toSavedVariableDefinition(patches.latest(definitionId).patchId, it) }
                // New validity period is always open-ended. A valid_until date may be set via a patch.
                .apply { validUntil = null }
                .let { patches.save(it) }
        }
    }

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
    fun getLatestPatchForValidityPeriod(
        definitionId: String,
        validFrom: LocalDate?,
    ): SavedVariableDefinition =
        listAllPatchesGroupedByValidityPeriods(definitionId)
            .let {
                // Get the validityPeriod matching the given validFrom.
                // If no validFrom is given, get the latest validityPeriod
                it[validFrom ?: it.keys.last()]
            }
            // If no matching Validity Period is found (null value), throw an exception
            // Get the latest patch in the matching Validity Period
            ?.last() ?: run {
            throw NoMatchingValidityPeriodFound("No Validity Period with valid_from date $validFrom")
        }
}

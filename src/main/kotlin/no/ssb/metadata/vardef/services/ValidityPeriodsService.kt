package no.ssb.metadata.vardef.services

import jakarta.inject.Singleton
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
            .listAllPatchesById(definitionId)
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
            .listAllPatchesById(definitionId)
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
                .toSavedVariableDefinition(patches.getLatestPatchById(definitionId).patchId, latestPatchInLastValidityPeriod),
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
}

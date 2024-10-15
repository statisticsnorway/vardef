package no.ssb.metadata.vardef.services

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
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
}

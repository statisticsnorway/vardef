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
}

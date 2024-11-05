package no.ssb.metadata.vardef.services

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.SavedVariableDefinition

@Singleton
class OwnerService(
    private val validityPeriodsService: ValidityPeriodsService,
    private val patchesService: PatchesService
) {

    fun patchOwner(definitionId: String, patch: Patch): List<SavedVariableDefinition?> {
        val validityPeriods =  validityPeriodsService.list(definitionId)
        if(patch.owner != null){
            return validityPeriods.map {
                patchesService.create(patch.toSavedVariableDefinition(it.patchId, it))
            }
        }
        return emptyList()
    }
}
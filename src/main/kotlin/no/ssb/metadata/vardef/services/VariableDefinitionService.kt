package no.ssb.metadata.vardef.services

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import java.time.LocalDate

@Singleton
class VariableDefinitionService(
    private val variableDefinitionRepository: VariableDefinitionRepository,
    private val klassService: KlassService,
    private val validityPeriods: ValidityPeriodsService,
) {
    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

    fun list(): List<SavedVariableDefinition> = variableDefinitionRepository.findAll()

    fun doesShortNameExist(shortName: String): Boolean = variableDefinitionRepository.existsByShortName(shortName)

    private fun uniqueDefinitionIds(): Set<String> =
        list()
            .map { it.definitionId }
            .toSet()

    fun listForDateAndRender(
        language: SupportedLanguages,
        dateOfValidity: LocalDate?,
    ): List<RenderedVariableDefinition> =
        uniqueDefinitionIds()
            .mapNotNull {
                getByDateAndRender(language, it, dateOfValidity)
            }

    fun getByDateAndRender(
        language: SupportedLanguages,
        definitionId: String,
        dateOfValidity: LocalDate?,
    ): RenderedVariableDefinition? =
        if (dateOfValidity == null) {
            validityPeriods.getLatestPatchInLastValidityPeriod(definitionId)
        } else {
            validityPeriods.getForDate(definitionId, dateOfValidity)
        }?.render(language, klassService)
}

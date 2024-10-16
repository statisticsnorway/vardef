package no.ssb.metadata.vardef.services

import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.extensions.isEqualOrAfter
import no.ssb.metadata.vardef.extensions.isEqualOrBefore
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import java.time.LocalDate

@Singleton
class VariableDefinitionService(
    private val variableDefinitionRepository: VariableDefinitionRepository,
    private val patches: PatchesService,
    private val validityPeriods: ValidityPeriodsService,
) {
    @Inject
    private lateinit var klassService: KlassService

    fun clear() = variableDefinitionRepository.deleteAll()

    fun listAll(): List<SavedVariableDefinition> =
        variableDefinitionRepository
            .findAll()
            .toList()

    fun listAllAndRenderForLanguage(
        language: SupportedLanguages,
        dateOfValidity: LocalDate?,
    ): List<RenderedVariableDefinition> {
        var definitionList = listAll()
        if (dateOfValidity != null) {
            definitionList =
                definitionList
                    .filter { dateOfValidity.isEqualOrAfter(it.validFrom) }
                    // If validUntil is null then this filter predicate should be true, so we just compare the date against itself.
                    .filter { dateOfValidity.isEqualOrBefore(it.validUntil ?: dateOfValidity) }
        }
        return definitionList
            .map {
                it.render(
                    language,
                    klassService,
                )
            }.groupBy {
                it.id
            }.mapValues { entry ->
                entry.value.maxBy { it.patchId }
            }.values
            .toList()
    }

    fun getOneByIdAndDateAndRenderForLanguage(
        language: SupportedLanguages,
        definitionId: String,
        dateOfValidity: LocalDate?,
    ): RenderedVariableDefinition =
        if (dateOfValidity != null) {
            validityPeriods.getForDate(definitionId, dateOfValidity).render(language, klassService)
        } else {
            validityPeriods.getLatestPatchInLastValidityPeriod(definitionId).render(language, klassService)
        }

    fun save(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.save(varDef)

    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

    fun deleteById(id: String): Any =
        patches
            .list(id)
            .map {
                variableDefinitionRepository.deleteById(it.id)
            }

    fun checkIfShortNameExists(shortName: String): Boolean = variableDefinitionRepository.findByShortName(shortName).isNotEmpty()
}

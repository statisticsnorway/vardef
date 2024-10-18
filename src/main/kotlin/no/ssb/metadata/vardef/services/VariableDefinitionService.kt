package no.ssb.metadata.vardef.services

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
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
    /**
     * Update a Draft Variable Definition
     *
     * @param varDef The object with updates applied
     * @return The same object
     */
    fun update(varDef: SavedVariableDefinition): SavedVariableDefinition = variableDefinitionRepository.update(varDef)

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
    fun doesShortNameExist(shortName: String): Boolean = variableDefinitionRepository.existsByShortName(shortName)

    private fun uniqueDefinitionIds(): Set<String> =
        list()
            .map { it.definitionId }
            .toSet()

    /**
     * List *Variable Definitions* which are valid on the given date.
     *
     * If no date is given, list all variable definitions.
     *
     * @param language The language to render in.
     * @param dateOfValidity The date which *Variable Definitions* shall be valid at.
     * @return List of *Variable Definitions* valid at the date.
     */
    fun listForDateAndRender(
        language: SupportedLanguages,
        dateOfValidity: LocalDate?,
    ): List<RenderedVariableDefinition> =
        uniqueDefinitionIds()
            .mapNotNull {
                getByDateAndRender(language, it, dateOfValidity)
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
     * @return The *Variable Definition* or `null`
     */
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

package no.ssb.metadata.vardef.services

import io.viascom.nanoid.NanoId
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.*
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
    ): List<RenderedVariableDefinition> =
        uniqueDefinitionIdsByStatus(VariableStatus.PUBLISHED_EXTERNAL)
            .mapNotNull {
                getPublicByDate(language, it, dateOfValidity)
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
    fun listCompleteForDate(dateOfValidity: LocalDate?): List<CompleteResponse> =
        uniqueDefinitionIds()
            .mapNotNull {
                getCompleteByDate(it, dateOfValidity)
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
     * @return The [RenderedVariableDefinition] or `null`
     */
    fun getPublicByDate(
        language: SupportedLanguages,
        definitionId: String,
        dateOfValidity: LocalDate?,
    ): RenderedVariableDefinition? =
        getByDateAndStatus(definitionId, dateOfValidity, VariableStatus.PUBLISHED_EXTERNAL)
            ?.render(language, klassService)

    private fun getByDateAndStatus(
        definitionId: String,
        dateOfValidity: LocalDate? = null,
        variableStatus: VariableStatus? = null,
    ): SavedVariableDefinition? =
        if (dateOfValidity == null) {
            validityPeriods.getLatestPatchInLastValidityPeriod(definitionId)
        } else {
            validityPeriods.getForDate(definitionId, dateOfValidity)
        }.takeIf {
            variableStatus == null ||
                it?.variableStatus == variableStatus
        }

    /**
     * Get a *Variable Definition*, valid on the given date.
     *
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

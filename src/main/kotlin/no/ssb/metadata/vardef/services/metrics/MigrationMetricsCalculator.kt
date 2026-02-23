package no.ssb.metadata.vardef.services.metrics

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.services.ValidityPeriodsService

@Singleton
class MigrationMetricsCalculator(
    private val vardokIdMappingRepository: VardokIdMappingRepository,
    private val validityPeriodsService: ValidityPeriodsService,
    private val daplaTeamService: DaplaTeamService,
) {
    /**
     * Counts migrated variable definitions grouped by their team's section code.
     */
    fun countMigratedVariablesBySection(): Map<String, Int> =
        migratedVariablesBySection()
            .mapValues { (_, variables) -> variables.size }

    /**
     *  Counts the number of edited migrated variables per section.
     *  A variable is considered edited if `createdAt` differs from `lastUpdatedAt`.
     */
    fun countEditedMigratedBySection(): Map<String, Int> =
        migratedVariablesBySection()
            .mapValues { (_, variables) ->
                variables.count { it.createdAt != it.lastUpdatedAt }
            }

    /**
     * Count total edited migrated variables
     */
    fun countTotalEditedMigrated(): Int =
        migratedVariablesBySection()
            .values // all lists of variables per section
            .flatten() // merge into a single list
            .count { it.createdAt != it.lastUpdatedAt }

    /**
     * Returns migrated variable definitions grouped by their team's section code.
     */
    private fun migratedVariablesBySection(): Map<String, List<SavedVariableDefinition>> =
        vardokIdMappingRepository
            .findAll()
            .map { (_, vardefId, _) ->
                validityPeriodsService
                    .getLatestPatchInLastValidityPeriod(vardefId)
            }.groupBy { variable ->
                val team = variable.owner.team
                daplaTeamService.getTeam(team)?.section?.code ?: "Unknown"
            }
}

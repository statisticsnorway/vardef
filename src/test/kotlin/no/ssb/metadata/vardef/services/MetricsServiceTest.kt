package no.ssb.metadata.vardef.services

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamApiService
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetricsServiceTest: BaseVardefTest()  {

    @Inject
    private lateinit var validityPeriodsService: ValidityPeriodsService

    @Inject
    private lateinit var daplaTeamApiService: DaplaTeamApiService

    private fun migratedVariablesBySection(): Map<String, List<SavedVariableDefinition>> =
        vardokIdMappingRepository
            .findAll()
            .map { (_, vardefId, _) ->
                validityPeriodsService
                    .getLatestPatchInLastValidityPeriod(vardefId)
            }.groupBy { variable ->
                val team = variable.owner.team
                daplaTeamApiService.getTeam(team)?.sectionCode ?: "Unknown"
            }

    private fun countMigratedVariablesBySection(): Map<String, Int> =
        migratedVariablesBySection()
            .mapValues { (_, variables) -> variables.size }

    private fun countEditedMigratedBySection(): Map<String, Int> =
        migratedVariablesBySection()
            .mapValues { (_, variables) ->
                variables.count { it.createdAt != it.lastUpdatedAt }
            }

    @Test
    fun `map migrated variables`() {
        val migrated = migratedVariablesBySection();
        assertThat(migrated.size).isEqualTo(1);
        assertThat(migrated.keys).containsExactlyInAnyOrder("724")
        assertThat(migrated["724"]?.get(0)).isInstanceOf(SavedVariableDefinition::class.java)
        assertThat(countMigratedVariablesBySection()["724"]).isEqualTo(1)

    }

    @Test
    fun `count edited migrated variables`() {
        val migrated = migratedVariablesBySection();
        assertThat(migrated.size).isEqualTo(1);
        assertThat(migrated.keys).containsExactlyInAnyOrder("724")
        assertThat(countEditedMigratedBySection()["724"]).isEqualTo(0)
    }


}
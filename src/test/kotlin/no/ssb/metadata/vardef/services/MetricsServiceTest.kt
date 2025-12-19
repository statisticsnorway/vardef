package no.ssb.metadata.vardef.services

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamApiService
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.UpdateDraft
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetricsServiceTest : BaseVardefTest() {
    @Inject
    private lateinit var validityPeriodsService: ValidityPeriodsService

    @Inject
    private lateinit var daplaTeamApiService: DaplaTeamApiService

    @BeforeAll
    fun setUpMetrics() {
        val variable = validityPeriodsService.getLatestPatchInLastValidityPeriod(DRAFT_BUS_EXAMPLE.definitionId)
        Thread.sleep(5)
        val updateDraft = UpdateDraft(name = LanguageStringType(nb="Et bedre navn", nn="Eit betre namn", en="A better name"))
        variableDefinitionService.update(variable, updateDraft, TEST_USER)
    }

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
        val migrated = migratedVariablesBySection()
        assertThat(migrated.size).isEqualTo(2)
        assertThat(migrated.keys).containsExactlyInAnyOrder("724", "Unknown")
        assertThat(migrated["724"]?.get(0)).isInstanceOf(SavedVariableDefinition::class.java)
        assertThat(countMigratedVariablesBySection()["724"]).isEqualTo(1)
    }

    @Test
    fun `count edited migrated variables`() {
        assertThat(DRAFT_BUS_EXAMPLE.createdAt).isNotEqualTo(DRAFT_BUS_EXAMPLE.lastUpdatedAt)
        assertThat(countEditedMigratedBySection()).isEqualTo("dvkt")
    }
}

package no.ssb.metadata.vardef.services

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPair
import no.ssb.metadata.vardef.services.metrics.MigrationMetricsCalculator
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MetricsServiceTest : BaseVardefTest() {
    @Inject
    private lateinit var metricsCalculator: MigrationMetricsCalculator

    @BeforeEach
    fun setUpMetrics() {
        vardokIdMappingRepository.save(VardokVardefIdPair("006", SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId))

        // DRAFT_BUS_EXAMPLE is edited after it was migrated
        val editedVariable =
            DRAFT_BUS_EXAMPLE.copy(
                lastUpdatedAt = DRAFT_BUS_EXAMPLE.lastUpdatedAt.plusSeconds(3600),
            )
        variableDefinitionRepository.update(editedVariable)
    }

    @Test
    fun `count migrated variables per section`() {
        val expected =
            mapOf(
                "724" to 1,
                "Unknown" to 1,
            )

        assertThat(vardokIdMappingRepository.count())
            .withFailMessage("Repository should contain 2 variable mappings")
            .isEqualTo(2)

        assertThat(metricsCalculator.countMigratedVariablesBySection())
            .withFailMessage(
                "Count of migrated variables per section ${metricsCalculator.countMigratedVariablesBySection()} should match expected: $expected",
            ).isEqualTo(expected)
    }

    @Test
    fun `count edited migrated variables per section`() {
        val expected =
            mapOf(
                "724" to 1,
                "Unknown" to 0,
            )
        assertThat(metricsCalculator.countEditedMigratedBySection())
            .withFailMessage(
                "Count of edited migrated variables per section ${metricsCalculator.countEditedMigratedBySection()} should match expected: $expected",
            ).isEqualTo(expected)

        assertThat(metricsCalculator.countTotalEditedMigrated())
            .withFailMessage("Total edited migrated should be 1")
            .isEqualTo(1)
    }
}

@Factory
@Replaces(DaplaTeamService::class)
class TestDaplaTeamFactory {
    @Singleton
    fun daplaTeamService(): DaplaTeamService =
        object : DaplaTeamService {
            override fun getTeam(teamName: String) =
                when (teamName) {
                    "play-enhjoern-a" -> Team("724", sectionCode = "724", sectionName = "Seksjon for dataplattform")
                    "skip-stat" -> Team("0", sectionCode = "Unknown", sectionName = "Unknown")
                    else -> null
                }

            override fun isValidTeam(teamName: String) = getTeam(teamName) != null

            override fun getGroup(groupName: String) = Group(groupName)

            override fun isValidGroup(groupName: String) = true
        }
}

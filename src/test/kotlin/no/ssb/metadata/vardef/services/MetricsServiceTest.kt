package no.ssb.metadata.vardef.services

import io.micronaut.context.annotation.Replaces
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamApiService
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamService
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPair
import no.ssb.metadata.vardef.services.metrics.MigrationMetricsCalculator
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class MetricsServiceTest : BaseVardefTest() {

    private val logger = LoggerFactory.getLogger(MetricsServiceTest::class.java)
    @Singleton
    @Replaces(DaplaTeamApiService::class)
    fun mockDaplaTeamService(): DaplaTeamService =
        object : DaplaTeamService {
            override fun getTeam(teamName: String): Team? {
                logger.info("Mock get Team for $teamName")
                return when (teamName) {
                    "play-enhjoern-a" -> Team("724", sectionCode = "724", sectionName = "Seksjon for dataplattform")
                    "skip-stat" -> Team("0", sectionCode = "Unknown", sectionName = "Unknown")
                    else -> null
                }
            }

            override fun isValidTeam(teamName: String) = true

            override fun getGroup(groupName: String) = Group(groupName)

            override fun isValidGroup(groupName: String) = true
        }

    @Inject
    private lateinit var metricsCalculator: MigrationMetricsCalculator

    @BeforeEach
    fun setUpMetrics() {
        // SAVED_DRAFT_DEADWEIGHT_EXAMPLE has not valid team -> will be counted as "Unknown"
        vardokIdMappingRepository.save(VardokVardefIdPair("006", SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId))

        // DRAFT_BUS_EXAMPLE is assigned to team 724
        // and is edited after it was migrated
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
            .withFailMessage("Count of migrated variables per section ${metricsCalculator.countMigratedVariablesBySection()} should match expected: $expected")
            .isEqualTo(expected)
    }

    @Test
    fun `count edited migrated variables per section`() {
        val expected =
            mapOf(
                "724" to 1,
                "Unknown" to 0,
            )
        assertThat(metricsCalculator.countEditedMigratedBySection())
            .withFailMessage("Count of edited migrated variables per section ${metricsCalculator.countEditedMigratedBySection()} should match expected: $expected")
            .isEqualTo(expected)

        assertThat(metricsCalculator.countTotalEditedMigrated())
            .withFailMessage("Total edited migrated should be 1")
            .isEqualTo(1)
    }
}

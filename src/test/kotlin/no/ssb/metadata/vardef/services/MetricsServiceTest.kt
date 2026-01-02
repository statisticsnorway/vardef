package no.ssb.metadata.vardef.services

import jakarta.inject.Inject
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
        // SAVED_DRAFT_DEADWEIGHT_EXAMPLE owner Team is "skip-stat"
        vardokIdMappingRepository.save(VardokVardefIdPair("006", SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId))

        // DRAFT_BUS_EXAMPLE owner Team is  "play-enhjoern-a"
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
                "play-enhjoern-a" to 1,
                "skip-stat" to 1,
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
                "play-enhjoern-a" to 1,
                "skip-stat" to 0,
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

package no.ssb.metadata.vardef.services

import jakarta.inject.Inject
import no.ssb.metadata.vardef.services.metrics.MigrationMetricsCalculator
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MetricsServiceTest : BaseVardefTest() {
    @Inject
    private lateinit var metricsCalculator: MigrationMetricsCalculator

    @Test
    fun `map migrated variables`() {
        val countMigrated = metricsCalculator.countMigratedVariablesBySection()
        assertThat(countMigrated.size).isEqualTo(2)
        assertThat(countMigrated.keys).containsExactlyInAnyOrder("724", "Unknown")
        assertThat(countMigrated["724"]).isEqualTo(1)
    }

    @Test
    fun `count edited migrated variables`() {
        assertThat(DRAFT_BUS_EXAMPLE.createdAt).isNotEqualTo(DRAFT_BUS_EXAMPLE.lastUpdatedAt)
        assertThat(metricsCalculator.countEditedMigratedBySection()).isEqualTo("dvkt")
    }
}

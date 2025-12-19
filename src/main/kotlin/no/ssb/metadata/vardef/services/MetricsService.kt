package no.ssb.metadata.vardef.services

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaTeamApiService
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

const val MIGRATED_COUNT_METRIC = "ssb.variable-definitions.migrated"
const val EDITED_MIGRATED_COUNT_METRIC = "ssb.variable.definitions.edited.migrated"
const val SECTION_TAG_KEY = "section"

@Requires(env = ["naistest", "naisprod"])
@Singleton
class MetricsService(
    private val vardokIdMappingRepository: VardokIdMappingRepository,
    private val validityPeriodsService: ValidityPeriodsService,
    private val daplaTeamApiService: DaplaTeamApiService,
    private val meterRegistry: MeterRegistry,
) {
    private val logger = LoggerFactory.getLogger(MetricsService::class.java)
    private val totalMigrated = AtomicInteger(0)

    private val migrated: MultiGauge =
        MultiGauge
            .builder(MIGRATED_COUNT_METRIC)
            .description("The number of migrated variable definitions, by section")
            .register(meterRegistry)

    private val editedMigrated: MultiGauge =
        MultiGauge
            .builder(EDITED_MIGRATED_COUNT_METRIC)
            .description("The number of migrated variable definitions which has been changed after migration, by section")
            .register(meterRegistry)

    init {
        meterRegistry.gauge(MIGRATED_COUNT_METRIC, Tags.of(SECTION_TAG_KEY, "total"), totalMigrated)
        meterRegistry.gauge(EDITED_MIGRATED_COUNT_METRIC, Tags.of(SECTION_TAG_KEY, "total"), totalMigrated)
    }

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
                daplaTeamApiService.getTeam(team)?.sectionCode ?: "Unknown"
            }

    /**
     * Returns migrated variables that have been edited, grouped by section code.
     * Only includes variables where `createdAt` differs from `lastUpdatedAt`.
     */
    private fun editedMigrated(): Map<String, List<SavedVariableDefinition>> =
        migratedVariablesBySection()
            .mapValues { (_, variables) -> variables.filter { it.createdAt != it.lastUpdatedAt } }

    private fun countMigratedVariablesBySection(): Map<String, Int> =
        migratedVariablesBySection()
            .mapValues { (_, variables) -> variables.size }

    /**
     *  Counts the number of edited migrated variables per section.
     *  A variable is considered edited if `createdAt` differs from `lastUpdatedAt`.
     */
    private fun countEditedMigratedBySection(): Map<String, Int> =
        migratedVariablesBySection()
            .mapValues { (_, variables) ->
                variables.count { it.createdAt != it.lastUpdatedAt }
            }

    @Scheduled(
        fixedRate = $$"${micronaut.metrics.custom.update-frequency:1h}",
        initialDelay = $$"${micronaut.metrics.custom.initial-delay:1h}",
    )
    fun exportMetrics() {
        totalMigrated.set(vardokIdMappingRepository.count().toInt())

        logger.debug("Updating metrics.")
        migrated.register(
            countMigratedVariablesBySection()
                .also { logger.debug(it.toString()) }
                .map {
                    MultiGauge.Row.of(Tags.of(SECTION_TAG_KEY, it.key), it.value)
                }.toList(),
            true,
        )

        editedMigrated.register(
            countEditedMigratedBySection()
                .also { logger.debug(it.toString()) }
                .map {
                    MultiGauge.Row.of(Tags.of(SECTION_TAG_KEY, it.key), it.value)
                }.toList(),
            true,
        )
    }
}

package no.ssb.metadata.vardef.services.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import io.micronaut.context.annotation.Requires
import io.micronaut.scheduling.annotation.Scheduled
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

const val MIGRATED_COUNT_METRIC = "ssb.variable-definitions.migrated"
const val EDITED_MIGRATED_COUNT_METRIC = "ssb.variable-definitions.edited-migrated"
const val SECTION_TAG_KEY = "section"

@Requires(env = ["naistest", "naisprod"])
@Singleton
class MetricsService(
    private val calculator: MigrationMetricsCalculator,
    private val vardokIdMappingRepository: VardokIdMappingRepository,
    private val meterRegistry: MeterRegistry,
) {
    private val logger = LoggerFactory.getLogger(MetricsService::class.java)
    private val totalMigrated = AtomicInteger(0)
    private val totalEdited = AtomicInteger(0)

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
        meterRegistry.gauge(EDITED_MIGRATED_COUNT_METRIC, Tags.of(SECTION_TAG_KEY, "total"), totalEdited)
    }

    @Scheduled(
        fixedRate = $$"${micronaut.metrics.custom.update-frequency:1h}",
        initialDelay = $$"${micronaut.metrics.custom.initial-delay:1h}",
    )
    fun exportMetrics() {
        totalMigrated.set(vardokIdMappingRepository.count().toInt())
        totalEdited.set(calculator.countTotalEditedMigrated())

        logger.debug("Updating metrics.")
        migrated.register(
            calculator
                .countMigratedVariablesBySection()
                .also { logger.debug(it.toString()) }
                .map {
                    MultiGauge.Row.of(Tags.of(SECTION_TAG_KEY, it.key), it.value)
                }.toList(),
            true,
        )

        editedMigrated.register(
            calculator
                .countEditedMigratedBySection()
                .also { logger.debug(it.toString()) }
                .map {
                    MultiGauge.Row.of(Tags.of(SECTION_TAG_KEY, it.key), it.value)
                }.toList(),
            true,
        )
    }
}

package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class KlassJob {
    @Scheduled(cron = "0 00 13 * * ?")
    fun getClassifications() {
        LOG.info("Simulate getting classifications: {}", SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassJob::class.java)
    }
}

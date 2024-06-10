package no.ssb.metadata.vardef.integrations.klass.scheduling

import io.micronaut.http.HttpResponse
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import org.slf4j.LoggerFactory
import java.net.SocketException
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class KlassApiCacheJob(private val klassApiService: KlassApiService) {
    @Scheduled(cron = "0 08 14 * * ?")
    fun runDailyKlassApiJob(): HttpResponse<KlassApiResponse>? {
        try {
            LOG.info("Executing daily request to Klass api")
            val result = klassApiService.runKlassApiJob()
            LOG.info(
                "Get classifications from Klass Api {} at {}",
                result,
                SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()),
            )
            return result
        } catch (e: Exception) {
            LOG.error("Error during Klass API", e)
            return null
        }
    }

    fun getClassificationsFromCache(): KlassApiResponse? {
        try {
            LOG.info("Executing scheduled test cache job")
            val result = klassApiService.classifications
            LOG.info("In cache {} at {}", result, SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()))
            return result
        } catch (e: SocketException) {
            e.printStackTrace()
            return null
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassApiCacheJob::class.java)
    }
}

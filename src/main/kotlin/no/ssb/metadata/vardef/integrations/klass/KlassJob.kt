package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.http.HttpResponse
import io.micronaut.scheduling.annotation.Scheduled
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@Singleton
class KlassJob {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    @Scheduled(cron = "0 44 12 * * ?")
    fun getClassifications(): HttpResponse<KlassApiResponse> {
        return try {
            val result = klassApiClient.fetchClassificationList()
            LOG.info("Retrieving classifications from Klass Api {} {}", result, SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()))
            HttpResponse.ok(result)
        } catch (e: Exception) {
            HttpResponse.notFound()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassJob::class.java)
    }
}

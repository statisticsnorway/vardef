package no.ssb.metadata.vardef.integrations.klass.service

import com.sun.net.httpserver.HttpServer
import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.retry.annotation.Retryable
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@CacheConfig("classifications")
@Singleton
open class KlassApiService(private val klassApiClient: KlassApiClient) {
    var klassApiResponse: KlassApiResponse? = null

    @Cacheable("classifications")
    open fun klassApiJob(): HttpResponse<KlassApiResponse> {
        return try {
            val result = klassApiClient.fetchClassificationList()
            LOG.info(
                "Retrieving classifications from Klass Api {}",
                SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date()),
            )
            this.klassApiResponse = result
            HttpResponse.ok(result)
        } catch (e: Exception) {
            LOG.warn("Error while fetching classifications from Klass Api", e)
            HttpResponse.serverError()
        }
    }

    @Retryable(delay = "2s", attempts = "3")
    open fun getClassifications(): KlassApiResponse {
        var attempts = 0
        if (this.klassApiResponse == null) {
            LOG.info("Request Klass Api at {}", SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date()))
            klassApiJob()
            if (klassApiJob().status == HttpStatus.INTERNAL_SERVER_ERROR || this.klassApiJob().status == HttpStatus.SERVICE_UNAVAILABLE) {
                attempts += 1
                klassApiJob()
            }
            return this.klassApiResponse!!
        }
        LOG.info("Fetching from cache at {}", SimpleDateFormat("dd/M/yyyy HH:mm:ss").format(Date()))
        return this.klassApiResponse!!
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassApiService::class.java)
    }
}

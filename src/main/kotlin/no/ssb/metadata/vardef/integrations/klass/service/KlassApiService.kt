package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.HttpResponse
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

@CacheConfig("classifications")
@Singleton
open class KlassApiService() {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    var classifications: KlassApiResponse? = null

    @Cacheable("classifications")
    open fun runKlassApiJob(): HttpResponse<KlassApiResponse> {
        return try {
            val result = klassApiClient.fetchClassificationList()
            LOG.info(
                "Retrieving classifications from Klass Api with cache support {} {}",
                result,
                SimpleDateFormat("dd/M/yyyy hh:mm:ss").format(Date()),
            )
            classifications = result
            HttpResponse.ok(result)
        } catch (e: Exception) {
            LOG.warn("Error while fetching classifications from Klass Api", e)
            HttpResponse.serverError()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassApiService::class.java)
    }
}

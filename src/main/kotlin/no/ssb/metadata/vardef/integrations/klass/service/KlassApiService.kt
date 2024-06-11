package no.ssb.metadata.vardef.integrations.klass.service
import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.slf4j.LoggerFactory

@CacheConfig("classifications")
@Singleton
open class KlassApiService(private val klassApiClient: KlassApiClient) {
    var klassApiResponse: KlassApiResponse? = null

    @Cacheable("classifications")
    open fun klassApiJob(): HttpResponse<KlassApiResponse> {
        return try {
            val result = klassApiClient.fetchClassificationList()
            LOG.info("Retrieving classifications from Klass Api")
            this.klassApiResponse = result
            HttpResponse.ok(result)
        } catch (e: Exception) {
            LOG.warn("Error while fetching classifications from Klass Api", e)
            HttpResponse.serverError()
        }
    }

    fun getClassifications(): KlassApiResponse? {
        if (this.klassApiResponse == null) {
            LOG.info("Request Klass Api")
            val response = klassApiJob()
            if (response.status == HttpStatus.OK) {
                return this.klassApiResponse!!
            }
            return null
        }
        LOG.info("Fetching from cache")
        return this.klassApiResponse!!
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(KlassApiService::class.java)
    }
}

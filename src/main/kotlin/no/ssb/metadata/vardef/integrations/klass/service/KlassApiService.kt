package no.ssb.metadata.vardef.integrations.klass.service
import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.Cacheable
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.slf4j.LoggerFactory

@CacheConfig("classifications")
@Singleton
open class KlassApiService(private val klassApiClient: KlassApiClient) {
    var klassApiResponse: KlassApiResponse? = null

    @Cacheable("classifications")
    open fun klassApiJob(): KlassApiResponse? {
        return try {
            val result = klassApiClient.fetchClassificationList()
            LOG.info("Retrieving classifications from Klass Api")
            this.klassApiResponse = result
            result
        } catch (e: Exception) {
            LOG.warn("Error while fetching classifications from Klass Api", e)
            return null
        }
    }

    fun getClassifications(): KlassApiResponse? {
        if (this.klassApiResponse == null) {
            LOG.info("Request Klass Api")
            val response = klassApiJob()
            if (response != null) {
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

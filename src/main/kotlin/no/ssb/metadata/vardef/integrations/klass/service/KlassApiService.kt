package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CachePut
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.slf4j.LoggerFactory

@CacheConfig("classifications")
@Singleton
open class KlassApiService(private val klassApiClient: KlassApiClient) {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)
    private val classifications = mutableMapOf<Int, Classification?>()
    var klassApiResponse: KlassApiResponse? = null

    @Property(name = "klass.cached-classifications.unit-types")
    private var unitTypesId: Int = 0

    @Property(name = "klass.cached-classifications.areas")
    private var areasId: Int = 0

    @Cacheable("classifications")
    open fun fetchClassifications(): HttpResponse<KlassApiResponse> {
        return try {
            val result = klassApiClient.fetchClassificationList()
            logger.info("Retrieving classifications from Klass Api")
            this.klassApiResponse = result
            HttpResponse.ok(result)
        } catch (e: Exception) {
            logger.warn("Error while fetching classifications from Klass Api", e)
            HttpResponse.serverError()
        }
    }

    fun getClassifications(): KlassApiResponse? {
        if (this.klassApiResponse == null) {
            logger.info("Request Klass Api")
            val response = fetchClassifications()
            if (response.status == HttpStatus.OK) {
                return this.klassApiResponse!!
            }
            return null
        }
        logger.info("Fetching from cache")
        return this.klassApiResponse!!
    }

    @CachePut("classifications", parameters = ["classificationId"])
    open fun getClassification(classificationId: Int): Classification? {
        return try {
            classifications.getOrDefault(classificationId, fetchClassification(classificationId))
        } catch (e: InterruptedException) {
            null
        }
    }

    private fun fetchClassification(classificationId: Int): Classification? {
        logger.info("Fetch classification and codes by id $classificationId from Klass Api")
        val classification = klassApiClient.fetchClassification(classificationId)
        val codes = klassApiClient.fetchCodeList(classificationId)

        if (classification == null) {
            logger.info("No classification found")
            return null
        }

        logger.info("Caching classification with id $classificationId")
        classifications[classificationId] = classification.copy(classificationItems = codes?.classificationItems)

        return classifications[classificationId]
    }

    fun getUnitTypes(): Classification? = getClassification(unitTypesId)

    fun getAreas(): Classification? = getClassification(areasId)
}

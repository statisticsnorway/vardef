package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CachePut
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@CacheConfig("classifications")
@Singleton
open class KlassApiService(private val klassApiClient: KlassApiClient) : KlassService {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)
    private val classificationCache: MutableMap<Int, Classification> = mutableMapOf()
    private val classificationItemListCache = mutableMapOf<Int, List<ClassificationItem>>()

    @Property(name = "klass.cache-retry-timeout-seconds")
    val timeout: Long = 360

    @Cacheable("classifications")
    open fun fetchAllClassifications(): List<Classification> {
        return try {
            logger.info("Retrieving classifications from Klass Api")
            klassApiClient.fetchClassifications()?.embedded?.classifications ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Error while fetching classifications from Klass Api", e)
            emptyList()
        }
    }

    @Cacheable("classifications")
    open fun getClassifications(): List<Classification> {
        if (hasExpiredCache()) {
            logger.info("Request Klass Api")
            fetchAllClassifications().forEach { classification ->
                classificationCache[classification.id] = classification
            }
        }
        logger.info("Fetching all classifications from cache")
        return classificationCache.values.toList()
    }

    @CachePut("classification", parameters = ["classificationId"])
    open fun getClassification(classificationId: Int): Classification {
        if (hasExpiredCache()) {
            getClassifications()
        }

        logger.info("Fetching classification with id $classificationId from cache")

        return classificationCache
            .getOrDefault(classificationId, Classification())
            .copy(classificationItems = getClassificationItemsById(classificationId))
    }

    @CachePut("ClassificationItems", parameters = ["classificationId"])
    open fun fetchClassificationItemsById(classificationId: Int): List<ClassificationItem> =
        try {
            logger.info("Retrieving classification items from Klass Api")
            klassApiClient.fetchCodeList(classificationId)?.classificationItems ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Error while fetching classification items from Klass Api", e)
            emptyList()
        }

    @CachePut("ClassificationItems", parameters = ["classificationId"])
    open fun getClassificationItemsById(classificationId: Int): List<ClassificationItem> {
        if (!classificationItemListCache.containsKey(classificationId)) {
            val classificationItems = fetchClassificationItemsById(classificationId)
            if (classificationItems.isNotEmpty()) {
                classificationItemListCache[classificationId] = classificationItems
            }
        }

        return classificationItemListCache.getOrDefault(classificationId, emptyList())
    }

    override fun getCodesFor(id: String): List<String> = getClassificationItemsById(id.toInt()).map { it.code }

    fun classificationCacheSize(): Int = classificationCache.size

    fun classificationItemListCache(): Int = classificationItemListCache.size

    private fun hasExpiredCache(): Boolean =
        classificationCache.isEmpty() ||
            LocalDateTime.now().plusSeconds(timeout) < classificationCache.values.first().lastFetched
}

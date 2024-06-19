package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CachePut
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.core.annotation.Introspected
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.slf4j.LoggerFactory

@CacheConfig("classifications")
@Singleton
@Introspected
open class KlassApiService(private val klassApiClient: KlassApiClient) : KlassService {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)
    private val classificationCache: MutableMap<Int, Classification> = mutableMapOf()
    private val classificationItemListCache = mutableMapOf<Int, List<ClassificationItem>>()

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
        if (classificationCache.isEmpty()) {
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
        if (classificationCache.isEmpty()) {
            getClassifications()
        }

        logger.info("Fetching classification with id $classificationId from cache")

        return classificationCache
            .getOrDefault(classificationId, Classification())
            .copy(classificationItems = getClassificationItemsById(classificationId))
    }

    @CachePut("ClassificationItems", parameters = ["classificationId"])
    open fun getClassificationItemsById(classificationId: Int): List<ClassificationItem> {
        if (!classificationItemListCache.containsKey(classificationId)) {
            try {
                logger.info("Retrieving classification items from Klass Api")
                classificationItemListCache[classificationId] =
                    klassApiClient
                        .fetchCodeList(classificationId)?.classificationItems
                        ?: emptyList()
            } catch (e: Exception) {
                logger.warn("Error while fetching classification items from Klass Api", e)
            }
        }

        return classificationItemListCache.getOrDefault(classificationId, emptyList())
    }

    override fun getCodesFor(id: String): List<String> = getClassificationItemsById(id.toInt()).map { it.code }
}

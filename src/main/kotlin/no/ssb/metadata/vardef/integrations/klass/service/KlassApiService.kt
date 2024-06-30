package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CachePut
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import io.micronaut.http.server.exceptions.HttpServerException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

@CacheConfig("classifications")
@Singleton
open class KlassApiService(private val klassApiClient: KlassApiClient) : KlassService {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)
    private var classificationCache: MutableMap<Int, Classification> = mutableMapOf()
    private val classificationItemListCache = mutableMapOf<Int, List<ClassificationItem>>()

    @Property(name = "klass.cache-retry-timeout-seconds")
    val timeout: Long = 360

    private val status500 = "Klass Api: Service is not available"

    @Cacheable("classifications")
    open fun fetchAllClassifications(): List<Classification>? {
        val notFound = "Klass Api: Classifications not found"

        try {
            logger.info("Klass Api: Fetching classifications")
            val response = klassApiClient.fetchClassifications()

            when (response.status.code) {
                500 -> {
                    logger.error(status500)
                    throw HttpServerException(status500)
                }
                404 -> {
                    logger.error(notFound)
                    throw HttpServerException(notFound)
                }
                else -> {
                    logger.info("Klass Api: Classifications fetched")
                    return response
                        .body()
                        ?.embedded
                        ?.classifications
                }
            }
        } catch (e: ClassCastException) {
            throw NoSuchElementException(notFound)
        }
    }

    @Cacheable("classifications")
    open fun getClassifications(): List<Classification> {
        if (cacheHasExpired()) {
            classificationCache = fetchAllClassifications()?.associateBy { it.id }?.toMutableMap() ?: mutableMapOf()
        }

        logger.info("Klass Api Service Cache: Getting all classifications")

        return classificationCache.values.toList()
    }

    @CachePut("classification", parameters = ["classificationId"])
    open fun getClassification(classificationId: Int): Classification {
        if (cacheHasExpired()) {
            getClassifications()
        }

        logger.info("Klass Api Service Cache: Getting classification with id $classificationId")

        return classificationCache[classificationId]
            ?.copy(classificationItems = getClassificationItemsById(classificationId))
            ?: throw NoSuchElementException("Klass Api Service Cache: No such classification with id $classificationId")
    }

    @CachePut("ClassificationItems", parameters = ["classificationId"])
    open fun fetchClassificationItemsById(classificationId: Int): List<ClassificationItem> {
        try {
            logger.info("Klass Api: Fetching classification items")
            val response = klassApiClient.fetchCodeList(classificationId)

            when (response.status.code) {
                500 -> {
                    logger.error(status500)
                    throw HttpServerException(status500)
                }
                200 -> logger.info("Klass Api: Classifications fetched")
                else -> {
                    logger.info("Klass Api: Classification items not found")
                    throw NoSuchElementException("Klass Api: No such classification items with id $classificationId")
                }
            }

            return response.body()?.classificationItems
                ?: throw NoSuchElementException("Klass Api: No such classification items with id $classificationId")
        } catch (e: ClassCastException) {
            throw NoSuchElementException("Klass Api: No such classification items with id $classificationId")
        }
    }

    @CachePut("ClassificationItems", parameters = ["classificationId"])
    open fun getClassificationItemsById(classificationId: Int): List<ClassificationItem> {
        if (cacheHasExpired()) {
            getClassifications()
        }

        if (!classificationCache.containsKey(classificationId)) {
            throw NoSuchElementException("Klass Api Service: No such classification items with id $classificationId")
        }

        if (!classificationItemListCache.containsKey(classificationId)) {
            val classificationItems = fetchClassificationItemsById(classificationId)
            if (classificationItems.isNotEmpty()) {
                classificationItemListCache[classificationId] = classificationItems
            }
        }

        return classificationItemListCache[classificationId]
            ?: throw NoSuchElementException(
                "Klass Api Service Cache: No such classification items with id $classificationId",
            )
    }

    override fun getCodesFor(id: String): List<String> = getClassificationItemsById(id.toInt()).map { it.code }

    fun classificationCacheSize(): Int = classificationCache.size

    fun classificationItemListCache(): Int = classificationItemListCache.size

    private fun cacheHasExpired(): Boolean =
        classificationCache.isEmpty() ||
            LocalDateTime.now().plusSeconds(timeout) < classificationCache.values.first().lastFetched
}

package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.CacheManager
import io.micronaut.cache.annotation.CacheInvalidate
import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.server.exceptions.HttpServerException
import io.micronaut.retry.annotation.CircuitBreaker
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.config.KlassConfiguration
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.integrations.klass.models.Codes
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.slf4j.LoggerFactory

const val CODES_CACHE = "codes"
const val CLASSIFICATIONS_CACHE = "classifications"
const val KLASS_NOT_FOUND_CACHE = "klass-not-found"

@Singleton
open class KlassApiService(
    private val klassApiClient: KlassApiClient,
    private val klassConfiguration: KlassConfiguration,
    private val cacheManager: CacheManager<Any>,
) : KlassService {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)

    @Property(name = "micronaut.klass-web.url.nb")
    private lateinit var klassUrlNb: String

    @Property(name = "micronaut.klass-web.url.en")
    private lateinit var klassUrlEn: String

    @CacheInvalidate(value = [CODES_CACHE, CLASSIFICATIONS_CACHE, KLASS_NOT_FOUND_CACHE], all = true)
    open fun invalidateCaches() = Unit

    @Cacheable(CLASSIFICATIONS_CACHE)
    open fun getClassification(classificationId: Int): Classification {
        val response = klassApiClient.fetchClassification(classificationId)
        handleErrorCodes(classificationId, response)
        return response.body()
            ?: throw NoSuchElementException("No content for Classification with ID $classificationId")
    }

    @Cacheable(CODES_CACHE)
    @CircuitBreaker(
        includes = [HttpClientException::class, HttpServerException::class],
        excludes = [KlassNotFoundException::class, NoSuchElementException::class],
        attempts = "2",
        delay = "100ms",
        maxDelay = "1s",
        reset = "30s",
    )
    open fun getCodeObjectsFor(
        classificationId: Int,
        language: SupportedLanguages,
        level: Int? = null,
    ): List<Code> {
        if (isInNotFoundCooldown(classificationId, language, level)) {
            throw KlassNotFoundException("Classification $classificationId not found")
        }

        logger.debug("Fetching codes for $classificationId")
        val codesAt = klassConfiguration.codesAtForClassification(classificationId.toString())
        val response: HttpResponse<Codes>

        try {
            response = if (level == null) {
                klassApiClient.listCodesAtDate(classificationId, codesAt, language)
            } else {
                klassApiClient.listCodesAtDateAndLevel(classificationId, codesAt, language, level)
            }

            handleErrorCodes(classificationId, response)
        } catch (e: KlassNotFoundException) {
            setNotFoundCooldown(classificationId, language, level)
            throw e
        }

        val codes = response.body()?.codes
        if (codes.isNullOrEmpty()) {
            throw NoSuchElementException(
                "No codes found for $classificationId",
            )
        }
        return codes
    }

    private fun <T : Any> handleErrorCodes(
        classificationId: Int,
        response: HttpResponse<T>,
    ): HttpResponse<T> {
        when (response.status.code) {
            500 -> {
                throw HttpServerException("$STATUS_500_MESSAGE classificationId $classificationId response $response")
            }

            404 -> {
                throw KlassNotFoundException("Classification $classificationId not found")
            }

            else -> {
                logger.info("Classification {} fetched", classificationId)
                return response
            }
        }
    }

    override fun getCodesFor(
        id: String,
        level: Int?,
    ): List<String> =
        getCodeObjectsFor(id.toInt(), SupportedLanguages.NB, level).map {
            it.code
        }

    override fun doesClassificationExist(id: String): Boolean =
        try {
            getClassification(id.toInt())
            true
        } catch (e: Exception) {
            false
        }

    override fun renderCode(
        classificationId: String,
        code: String,
        language: SupportedLanguages,
    ): KlassReference {
        var codeObject: Code?
        try {
            codeObject =
                getCodeObjectsFor(classificationId.toInt(), language)
                    .firstOrNull { it.code == code }
        } catch (e: KlassNotFoundException) {
            logger.error("Classification $classificationId not available for language $language", e)
            codeObject = null
        } catch (e: NoSuchElementException) {
            logger.error("Classification $classificationId not available for language $language", e)
            codeObject = null
        } catch (e: Exception) {
            logger.error("Failed to fetch classification $classificationId for language $language", e)
            codeObject = null
        }

        return KlassReference(
            getKlassUrlForIdAndLanguage(classificationId, language),
            codeObject?.code ?: code,
            codeObject?.name,
        )
    }

    override fun getKlassUrlForIdAndLanguage(
        classificationId: String,
        language: SupportedLanguages,
    ): String {
        val baseUrl =
            when (language) {
                SupportedLanguages.NB, SupportedLanguages.NN -> klassUrlNb
                SupportedLanguages.EN -> klassUrlEn
            }
        return "$baseUrl/klassifikasjoner/$classificationId"
    }

    companion object {
        private const val STATUS_500_MESSAGE = "Service is not available"
    }

    private fun isInNotFoundCooldown(
        classificationId: Int,
        language: SupportedLanguages,
        level: Int?,
    ): Boolean =
        cacheManager
            .getCache(KLASS_NOT_FOUND_CACHE)
            .get(notFoundKey(classificationId, language, level), Boolean::class.java)
            .orElse(false)

    private fun setNotFoundCooldown(
        classificationId: Int,
        language: SupportedLanguages,
        level: Int?,
    ) {
        cacheManager
            .getCache(KLASS_NOT_FOUND_CACHE)
            .put(notFoundKey(classificationId, language, level), true)
    }

    private fun notFoundKey(
        classificationId: Int,
        language: SupportedLanguages,
        level: Int?,
    ): String = "$classificationId:$language:${level ?: "all"}"
}

class KlassNotFoundException(
    message: String,
) : NoSuchElementException(message)

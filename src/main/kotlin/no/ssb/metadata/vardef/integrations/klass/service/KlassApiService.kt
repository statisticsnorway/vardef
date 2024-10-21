package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.Cacheable
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.HttpServerException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.slf4j.LoggerFactory

@Singleton
open class KlassApiService(
    private val klassApiClient: KlassApiClient,
    @Property(name = "micronaut.http.services.klass.codes-at")
    private val codesAt: String,
    @Property(name = "micronaut.klass-web.url.nb")
    private var klassUrlNb: String,
    @Property(name = "micronaut.klass-web.url.en")
    private var klassUrlEn: String,
) : KlassService {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)

    @Cacheable("classifications")
    open fun getClassification(classificationId: Int): Classification {
        val response = klassApiClient.fetchClassification(classificationId)
        handleErrorCodes(classificationId, response)
        return response.body()
            ?: throw NoSuchElementException("No content for Classification with ID $classificationId")
    }

    @Cacheable("codes")
    open fun getCodeObjectsFor(
        classificationId: Int,
        language: SupportedLanguages,
    ): List<Code> {
        logger.info("Fetching codes for $classificationId")
        val response = klassApiClient.listCodes(classificationId, codesAt, language)
        handleErrorCodes(classificationId, response)
        return response.body().codes.ifEmpty {
            throw NoSuchElementException(
                "No codes found for $classificationId",
            )
        }
    }

    private fun <T : Any?> handleErrorCodes(
        classificationId: Int,
        response: HttpResponse<T>,
    ): HttpResponse<T> {
        when (response.status.code) {
            500 -> {
                logger.error(Companion.STATUS_500_MESSAGE)
                throw HttpServerException(Companion.STATUS_500_MESSAGE)
            }

            404 -> {
                logger.info("Classification $classificationId not found")
                throw NoSuchElementException("Classification $classificationId not found")
            }

            else -> {
                logger.info("Classification fetched")
                return response
            }
        }
    }

    override fun getCodesFor(id: String): List<String> = getCodeObjectsFor(id.toInt(), SupportedLanguages.NB).map { it.code }

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
    ): KlassReference? {
        var codeObject: Code?
        try {
            codeObject =
                getCodeObjectsFor(classificationId.toInt(), language)
                    .firstOrNull { it.code == code }
            // In this case the code doesn't exist in the code list
            if (codeObject == null) return null
        } catch (e: NoSuchElementException) {
            logger.warn("Classification $classificationId no available for language $language")
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
}

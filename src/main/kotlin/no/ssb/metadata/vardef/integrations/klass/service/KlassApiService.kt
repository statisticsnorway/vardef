package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.cache.annotation.CachePut
import io.micronaut.context.annotation.Property
import io.micronaut.http.server.exceptions.HttpServerException
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.slf4j.LoggerFactory

@CacheConfig(cacheNames = ["classifications", "codes"])
@Singleton
open class KlassApiService(
    private val klassApiClient: KlassApiClient,
    @Property(name = "micronaut.http.services.klass.codes-at")
    private val codesAt: String,
) : KlassService {
    private val logger = LoggerFactory.getLogger(KlassApiService::class.java)

    @Property(name = "klass.cache-retry-timeout-seconds")
    private val timeout: Long = 360
    private val status500 = "Klass Api: Service is not available"

    @Property(name = "micronaut.klass-web.url.nb")
    private var klassUrlNb: String = ""

    @Property(name = "micronaut.klass-web.url.en")
    private var klassUrlEn: String = ""

    private var classifications: MutableMap<Int, Classification> = mutableMapOf()
    private var codes: MutableMap<Int, MutableMap<SupportedLanguages, List<Code>>> = mutableMapOf()

    @CachePut("classifications", parameters = ["classificationId"])
    open fun getClassification(classificationId: Int): Classification =
        classifications[classificationId] ?: run {
            val freshClassification =
                klassApiClient.fetchClassification(classificationId).body()
                    ?: throw NoSuchElementException("Klass API: Classification with ID $classificationId not found")
            classifications[classificationId] = freshClassification
            return freshClassification
        }

    @CachePut("codes", parameters = ["classificationId"])
    open fun getCodeObjectsFor(
        classificationId: Int,
        language: SupportedLanguages,
    ): List<Code> {
        return codes[classificationId]?.get(language) ?: run {
            logger.info("Klass Api: Fetching codes for $classificationId")
            val response = klassApiClient.listCodes(classificationId, codesAt, language)

            when (response.status.code) {
                500 -> {
                    logger.error(status500)
                    throw HttpServerException(status500)
                }

                404 -> {
                    logger.info("Klass Api: Classification items not found")
                    throw NoSuchElementException("Klass Api: No such classification items with id $classificationId")
                }

                else -> {
                    logger.info("Klass Api: Classifications fetched")
                    val freshCodes =
                        response.body().codes.ifEmpty {
                            throw NoSuchElementException(
                                "Klass Api: No codes found for $classificationId",
                            )
                        }
                    if (!codes.containsKey(classificationId)) {
                        codes[classificationId] = mutableMapOf(language to freshCodes)
                    } else {
                        codes[classificationId]?.put(language, freshCodes)
                    }
                    return freshCodes
                }
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
        id: String,
        code: String,
        language: SupportedLanguages,
    ): KlassReference? {
        val classificationId = id.toInt()
        val classification = getCodeObjectsFor(classificationId, language).find { it.code == code }
        return classification?.let {
            val name = it.name
            KlassReference(
                getKlassUrlForIdAndLanguage(id, language),
                it.code,
                name,
            )
        }
    }

    override fun getKlassUrlForIdAndLanguage(
        id: String,
        language: SupportedLanguages,
    ): String {
        val baseUrl =
            when (language) {
                SupportedLanguages.NB, SupportedLanguages.NN -> klassUrlNb
                SupportedLanguages.EN -> klassUrlEn
            }
        return "$baseUrl/klassifikasjoner/$id"
    }
}

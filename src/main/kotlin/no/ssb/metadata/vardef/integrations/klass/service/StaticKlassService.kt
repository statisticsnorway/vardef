package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.*
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.json.JsonMapper
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Code
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SupportedLanguages
import java.nio.file.Path

const val KLASS_CLASSIFICATIONS_PROPERTY_NAME = "klass.classifications"

@Serdeable
data class StaticKlassCode(
    val code: String,
    val name: LanguageStringType,
)

// Deserializes test data from property in application-test.yaml
@Serdeable
@Requires(env = ["test"], notEnv = ["integration-test"], property = KLASS_CLASSIFICATIONS_PROPERTY_NAME)
@EachProperty(KLASS_CLASSIFICATIONS_PROPERTY_NAME)
class StaticClassification(
    @param:Parameter val id: String,
    @Property(name = "klass.static-data-path")
    private val path: Path,
    jsonMapper: JsonMapper,
) {
    var name: LanguageStringType? = null
    var codes: List<Code>? = null

    init {
        val resourcePath = path.resolve("$id.json").toString()
        val resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath)
        if (resource == null) {
            throw RuntimeException("Resource not found: $resourcePath")
        } else {
            val classification = jsonMapper.readValue(resource.readText(), Classification::class.java)
            name = LanguageStringType(nb = classification.name, en = null, nn = null)
            codes = classification.codes
        }
    }
}

@Primary
@Requires(env = ["test"], notEnv = ["integration-test"], property = KLASS_CLASSIFICATIONS_PROPERTY_NAME)
@Singleton
class StaticKlassService(
    private val beanContext: BeanContext,
) : KlassService {
    @Property(name = "micronaut.klass-web.url.nb")
    private var klassUrlNb: String = ""

    @Property(name = "micronaut.klass-web.url.en")
    private var klassUrlEn: String = ""

    override fun getCodesFor(id: String): List<String> {
        val classification: StaticClassification =
            beanContext.getBean(StaticClassification::class.java, Qualifiers.byName(id))
        return classification.codes
            ?.map { it.code }
            .orEmpty()
            .toList()
    }

    override fun doesClassificationExist(id: String): Boolean =
        try {
            beanContext.getBean(StaticClassification::class.java, Qualifiers.byName(id))
            true
        } catch (e: Exception) {
            false
        }

    override fun renderCode(
        classificationId: String,
        code: String,
        language: SupportedLanguages,
    ): KlassReference {
        val classification: StaticClassification =
            beanContext.getBean(StaticClassification::class.java, Qualifiers.byName(classificationId))

        val klassCode = classification.codes?.find { it.code == code }
        val name = if (language == SupportedLanguages.NB) klassCode?.name else null
        return KlassReference(getKlassUrlForIdAndLanguage(classificationId, language), klassCode?.code ?: code, name)
    }

    override fun getKlassUrlForIdAndLanguage(
        classificationId: String,
        language: SupportedLanguages,
    ): String {
        val baseUrl =
            when (language) {
                SupportedLanguages.NB -> klassUrlNb
                SupportedLanguages.NN -> klassUrlNb
                else -> klassUrlEn
            }
        return "$baseUrl/klassifikasjoner/$classificationId"
    }
}

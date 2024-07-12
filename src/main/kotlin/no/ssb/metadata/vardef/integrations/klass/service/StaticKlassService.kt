package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.*
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SupportedLanguages

const val KLASS_CLASSIFICATIONS_PROPERTY_NAME = "klass.classifications"

@Serdeable
data class StaticKlassCode(
    val code: String,
    val name: LanguageStringType,
)

// Deserializes test data from property in application-test.yaml
@Serdeable
@Requires(env = ["test"], property = KLASS_CLASSIFICATIONS_PROPERTY_NAME)
@EachProperty(KLASS_CLASSIFICATIONS_PROPERTY_NAME)
class StaticClassification(
    @param:Parameter val id: String,
) {
    var name: LanguageStringType? = null
    var codes: List<StaticKlassCode>? = null
}

@Primary
@Requires(env = ["test"], property = KLASS_CLASSIFICATIONS_PROPERTY_NAME)
@Singleton
class StaticKlassService : KlassService {
    @Inject
    lateinit var beanContext: BeanContext

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

    override fun getAllIds(): Collection<String> {
        val classifications: Collection<StaticClassification> =
            beanContext.getBeansOfType(StaticClassification::class.java).toList()
        return classifications.map { it.id }
    }

    override fun getCodeItemFor(
        id: String,
        code: String,
        language: SupportedLanguages,
    ): KlassReference? {
        val classification: StaticClassification =
            beanContext.getBean(StaticClassification::class.java, Qualifiers.byName(id))

        val klassCode = classification.codes?.find { it.code == code }
        return klassCode?.let {
            val name = if (language == SupportedLanguages.NB) it.name.nb else null
            KlassReference(getKlassUrlForIdAndLanguage(id, language), it.code, name)
        }
    }

    override fun getKlassUrlForIdAndLanguage(
        id: String,
        language: SupportedLanguages,
    ): String {
        val baseUrl =
            when (language) {
                SupportedLanguages.NB -> klassUrlNb
                SupportedLanguages.NN -> klassUrlNb
                else -> klassUrlEn
            }
        return "$baseUrl/klassifikasjoner/$id"
    }
}

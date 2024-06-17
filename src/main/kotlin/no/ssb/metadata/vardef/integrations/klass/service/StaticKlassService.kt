package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.EachProperty
import io.micronaut.context.annotation.Parameter
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.models.LanguageStringType

@Serdeable
data class StaticKlassCode(
    val code: String,
    val name: LanguageStringType,
)

// @ConfigurationProperties("klass.classifications")
@Serdeable
@EachProperty("klass.classifications")
class StaticClassification(
    @param:Parameter val id: String,
) {
    var name: LanguageStringType? = null
    var codes: List<StaticKlassCode>? = null
}

@Primary
@Requires(env = ["test"])
@Singleton
class StaticKlassService : KlassService {
    @Inject
    lateinit var beanContext: BeanContext

    override fun getCodesFor(id: String): List<String> {
        val classification: StaticClassification = beanContext.getBean(StaticClassification::class.java, Qualifiers.byName(id))
        println(classification)
        return classification.codes?.map { it.code }.orEmpty().toList()
    }
}

package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import jakarta.validation.constraints.Email

@Serdeable
data class LanguageStringType(
    val nb: String?,
    val nn: String?,
    val en: String?,
) {
    fun getValidLanguage(language: SupportedLanguages): String? =
        when (language) {
            SupportedLanguages.NB -> nb
            SupportedLanguages.NN -> nn
            SupportedLanguages.EN -> en
        }

    fun listPresentLanguages(): List<String> {
        val presentLanguages = mutableListOf<String>()
        if (nb != null) presentLanguages.add("nb")
        if (nn != null) presentLanguages.add("nn")
        if (en != null) presentLanguages.add("en")
        return presentLanguages
    }
}

@Serdeable(naming = SnakeCaseStrategy::class)
data class KlassReference(
    val referenceUri: String,
    val code: String?,
    val title: String?,
)

@Serdeable(naming = SnakeCaseStrategy::class)
data class Contact(
    val title: LanguageStringType,
    @Email
    val email: String,
)

@Serdeable(naming = SnakeCaseStrategy::class)
data class Person(
    val code: String,
    val name: String,
)

@Serdeable(naming = SnakeCaseStrategy::class)
data class Owner(
    var code: String,
    val name: String,
)

@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedContact(
    val title: String?,
    val email: String,
)

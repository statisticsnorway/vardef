package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.Email
import java.net.URL

@Serdeable
data class LanguageStringType(
    val nb: String?,
    val nn: String?,
    val en: String?,
) {
    fun getValidLanguage(language: SupportedLanguages): String? {
        return when (language) {
            SupportedLanguages.NB -> nb
            SupportedLanguages.NN -> nn
            SupportedLanguages.EN -> en
        }
    }
}

@Serdeable()
data class KlassReference(
    val referenceUri: URL,
    val code: String,
    val title: String,
)

@Serdeable()
data class Contact(
    val title: LanguageStringType,
    @Email
    val email: String,
)

@Serdeable()
data class Person(
    val code: String,
    val name: String,
)

@Serdeable()
data class Owner(
    val code: String,
    val name: String,
)

@Serdeable()
data class RenderedContact(
    val title: String?,
    val email: String,
)

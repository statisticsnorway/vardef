package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.Email

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
data class KlassReference (
    val referenceUri: String,
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
    @Email
    val email: String,
)


package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.LowerCaseStrategy

@Serdeable(naming = LowerCaseStrategy::class)
data class LanguageStringType(
    val nb: String?,
    val nn: String?,
    val en: String?
) {
    fun getValidLanguage(language: SupportedLanguages): String? {
        return when (language) {
            SupportedLanguages.NB -> nb
            SupportedLanguages.NN -> nn
            SupportedLanguages.EN -> en
        }
    }
}




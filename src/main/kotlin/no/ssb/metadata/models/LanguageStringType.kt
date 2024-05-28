package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.LowerCaseStrategy

enum class SupportedLanguages(val code: String) {
    NORSK_BOKMÅL("nb"),
    NORSK_NYNORSK("nn"),
    ENGLISH("en");

    override fun toString(): String {
        return code
    }
}

@Serdeable(naming = LowerCaseStrategy::class)
data class LanguageStringType(
    val nb: String?,
    val nn: String?,
    val en: String?
) {
    fun getValidLanguage(language: SupportedLanguages): String? {
        return when (language) {
            SupportedLanguages.NORSK_BOKMÅL -> nb
            SupportedLanguages.NORSK_NYNORSK -> nn
            SupportedLanguages.ENGLISH -> en
        }
    }
}




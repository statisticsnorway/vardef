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
    fun getValidLanguage(language: String): String? {
        when (language) {
            SupportedLanguages.NORSK_BOKMÅL.toString()-> return nb
            SupportedLanguages.NORSK_NYNORSK.toString() -> return nn
            SupportedLanguages.ENGLISH.toString() -> return en
        }
        return null
    }
}




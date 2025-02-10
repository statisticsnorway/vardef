package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class LanguageStringType(
    val nb: String?,
    val nn: String?,
    val en: String?,
) {
    fun getValue(language: SupportedLanguages): String? =
        when (language) {
            SupportedLanguages.NB -> nb
            SupportedLanguages.NN -> nn
            SupportedLanguages.EN -> en
        }

    fun oneOrMoreLanguagesPresent(): Boolean =
        SupportedLanguages.entries.any {
            this.isLanguagePresent(it)
        }

    fun allLanguagesPresent(): Boolean =
        SupportedLanguages.entries.all {
            this.isLanguagePresent(it)
        }

    fun isLanguagePresent(language: SupportedLanguages): Boolean = this.getValue(language).let { !it?.trim().isNullOrEmpty() }

    /**
     * Returns a list of languages currently present in the object,
     * based on non-null *LanguageStringType* fields.
     *
     * This function checks each language field (e.g., "nb", "nn", "en")
     * and adds the corresponding language code to the list if the field is not null.
     *
     * @return A list of language codes representing the present languages.
     */
    fun listPresentLanguages(): List<SupportedLanguages> = SupportedLanguages.entries.filter { this.isLanguagePresent(it) }

    fun update(updated: LanguageStringType): LanguageStringType =
        this.copy(
            nb = updated.nb ?: nb,
            nn = updated.nn ?: nn,
            en = updated.en ?: en,
        )
}

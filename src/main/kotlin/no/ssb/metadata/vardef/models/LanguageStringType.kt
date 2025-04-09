package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy

/**
 * Language string type
 *
 * Represents one text, with translations for the languages in [SupportedLanguages]. All fields
 * are nullable to allow for flexibility for maintainers.
 *
 * @property nb Norwegian Bokmål
 * @property nn Norwegian Nynorsk
 * @property en English
 * @constructor Create empty Language string type
 */
@Serdeable(naming = SnakeCaseStrategy::class)
data class LanguageStringType(
    var nb: String?,
    var nn: String?,
    var en: String?,
) {

    /**
     * Get value for language
     *
     * @param language the desired language
     * @return the value for the desired language
     */
    fun getValue(language: SupportedLanguages): String? =
        when (language) {
            SupportedLanguages.NB -> nb
            SupportedLanguages.NN -> nn
            SupportedLanguages.EN -> en
        }

    /**
     * Are one or more languages present?
     */
    fun oneOrMoreLanguagesPresent(): Boolean =
        SupportedLanguages.entries.any {
            this.isLanguagePresent(it)
        }

    /**
     * Are all languages present?
     */
    fun allLanguagesPresent(): Boolean =
        SupportedLanguages.entries.all {
            this.isLanguagePresent(it)
        }

    /**
     * Is the given language present?
     */
    fun isLanguagePresent(language: SupportedLanguages): Boolean = this.getValue(language).let { !it?.trim().isNullOrEmpty() }

    /**
     * List present languages
     */
    fun listPresentLanguages(): List<SupportedLanguages> = SupportedLanguages.entries.filter { this.isLanguagePresent(it) }

    /**
     * Update
     *
     * Merge with an update object, preferring non-null values
     *
     * @param updates the updates
     * @return the merged object
     */
    fun update(updates: LanguageStringType): LanguageStringType =
        this.copy(
            nb = updates.nb?.trim() ?: nb?.trim(),
            nn = updates.nn?.trim() ?: nn?.trim(),
            en = updates.en?.trim() ?: en?.trim(),
        )
    companion object {
        fun from(obj: LanguageStringType): LanguageStringType = LanguageStringType(
            obj.nb?.trim(),
            obj.nn?.trim(),
            obj.en?.trim()
        )
    }

}

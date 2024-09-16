package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import no.ssb.metadata.vardef.constants.CONTACT_EXAMPLE
import no.ssb.metadata.vardef.constants.KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE
import no.ssb.metadata.vardef.constants.PERSON_EXAMPLE

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

    /**
     * Returns a list of languages currently present in the object,
     * based on non-null *LanguageStringType* fields.
     *
     * This function checks each language field (e.g., "nb", "nn", "en")
     * and adds the corresponding language code to the list if the field is not null.
     *
     * @return A list of language codes representing the present languages.
     */
    fun listPresentLanguages(): List<SupportedLanguages> {
        val presentLanguages = mutableListOf<SupportedLanguages>()
        if (nb != null) presentLanguages.add(SupportedLanguages.NB)
        if (nn != null) presentLanguages.add(SupportedLanguages.NN)
        if (en != null) presentLanguages.add(SupportedLanguages.EN)
        return presentLanguages
    }
}

@Schema(examples = [KLASS_REFERENCE_SUBJECT_FIELD_EXAMPLE])
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

@Schema(example = PERSON_EXAMPLE)
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

@Schema(example = CONTACT_EXAMPLE)
@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedContact(
    val title: String?,
    val email: String,
)

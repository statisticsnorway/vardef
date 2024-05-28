package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import no.ssb.metadata.exceptions.UnknownLanguageException
import java.util.*

@Serdeable
enum class SupportedLanguages {
    @JsonProperty("nb")
    NB,

    @JsonProperty("nn")
    NN,

    @JsonProperty("en")
    EN,
    ;

    override fun toString() = name.lowercase()
}

@Singleton
class SupportedLanguagesConverter : TypeConverter<String, SupportedLanguages> {
    override fun convert(
        code: String,
        targetType: Class<SupportedLanguages>,
        context: ConversionContext,
    ): Optional<SupportedLanguages> {
        return Optional.of(
            SupportedLanguages.entries.firstOrNull {
                it.toString() == code
            } ?: throw UnknownLanguageException(
                "Unknown language code $code. Valid values are ${SupportedLanguages.entries.map { it.toString() }}",
            ),
        )
    }
}

package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable
@Schema(description = "Languages the application supports.")
enum class SupportedLanguages {
    @JsonProperty("nb")
    @Schema(description = "Norwegian Bokmål")
    NB,

    @JsonProperty("nn")
    @Schema(description = "Norwegian Nynorsk")
    NN,

    @JsonProperty("en")
    @Schema(description = "English")
    EN,
    ;

    override fun toString() = name.lowercase()
}

package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

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

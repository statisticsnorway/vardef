package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable
@Schema(description = "Supported variable status values")
enum class VariableStatus {
    @JsonProperty("draft")
    DRAFT,

    @JsonProperty("published internal")
    PUBLISHED_INTERNAL,

    @JsonProperty("published external")
    PUBLISHED_EXTERNAL,

    @JsonProperty("deprecated")
    DEPRECATED,
    ;

    override fun toString() = name.lowercase()
}

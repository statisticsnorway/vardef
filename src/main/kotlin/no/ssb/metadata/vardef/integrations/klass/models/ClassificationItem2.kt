package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ClassificationItem2(
    @JsonProperty val code: String,
    @JsonProperty val parentCode: String?,
    @JsonProperty val level: Int,
    @JsonProperty val name: String,
    @JsonProperty val shortName: String,
    @JsonProperty val presentationName: String,
    @JsonProperty val validFrom: String,
    @JsonProperty val validTo: String?,
    @JsonProperty val notes: String?,
)

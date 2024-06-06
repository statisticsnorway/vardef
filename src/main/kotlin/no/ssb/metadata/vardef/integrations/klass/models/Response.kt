package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Response(
    @JsonProperty("_embedded") val embedded: Embedded,
    @JsonProperty("_links") val links: Links,
    @JsonProperty("page") val page: Page
)
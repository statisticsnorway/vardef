package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Classification(
    @JsonProperty("name") val name: String,
    @JsonProperty("id") val id: Int,
    @JsonProperty("classificationType") val classificationType: String,
    @JsonProperty("lastModified") val lastModified: String,
    @JsonProperty("_links") val links: Links
)


@Serdeable
data class Page(
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Int,
    @JsonProperty("totalPages") val totalPages: Int,
    @JsonProperty("number") val number: Int
)




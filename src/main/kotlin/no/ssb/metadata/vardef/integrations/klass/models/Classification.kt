package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class KlassApiResponse(
    @JsonProperty("_embedded") val embedded: Classifications,
    @JsonProperty("_links") val links: Links,
    @JsonProperty("page") val page: Page
)

@Serdeable
data class Classifications(
    @JsonProperty("classifications") val classificationItems: List<ClassificationItem>,
)

@Serdeable
data class ClassificationItem(
    @JsonProperty("name") val name: String,
    @JsonProperty("id") val id: Int,
    @JsonProperty("classificationType") val classificationType: String,
    @JsonProperty("lastModified") val lastModified: String,
    @JsonProperty("_links") val links: Links?,
)

@Serdeable
data class Links(
    @JsonProperty("self") val self: Link?,
    @JsonProperty("first") val first: Link?,
    @JsonProperty("next") val next: Link?,
    @JsonProperty("last") val last: Link?,
    @JsonProperty("search") val search: Link?,
)

@Serdeable
data class Link(
    @JsonProperty("href") val href: String,
)

@Serdeable
data class Page(
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Int,
    @JsonProperty("totalPages") val totalPages: Int,
    @JsonProperty("number") val number: Int,
)

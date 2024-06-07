package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

/**
 * Data classes for Klass Api response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class KlassApiResponse(
    @JsonProperty("_embedded") val embedded: Classifications,
    @JsonProperty("_links") val links: PaginationLinks,
    @JsonProperty("page") val page: Page,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Classifications(
    @JsonProperty("classifications") val classificationItems: List<ClassificationItem>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class ClassificationItem(
    @JsonProperty("name") val name: String?,
    @JsonProperty("id") val id: Int?,
    @JsonProperty("classificationType") val classificationType: String?,
    @JsonProperty("lastModified") val lastModified: String?,
    @JsonProperty("_links") val links: ClassificationLinks?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class PaginationLinks(
    @JsonProperty("self") val self: Link?,
    @JsonProperty("first") val first: Link?,
    @JsonProperty("next") val next: Link?,
    @JsonProperty("last") val last: Link?,
    @JsonProperty("search") val search: Link?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class ClassificationLinks(
    @JsonProperty("self") val self: Link?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Link(
    @JsonProperty("href") val href: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Page(
    @JsonProperty("size") val size: Int,
    @JsonProperty("totalElements") val totalElements: Int,
    @JsonProperty("totalPages") val totalPages: Int,
    @JsonProperty("number") val number: Int,
)

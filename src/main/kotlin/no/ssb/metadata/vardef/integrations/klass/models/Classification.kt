package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate

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
    @JsonProperty("classifications") val classifications: List<Classification>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Classification(
    @JsonProperty("name") val name: String?,
    @JsonProperty("id") val id: Int?,
    @JsonProperty("classificationType") val classificationType: String?,
    @JsonProperty("lastModified") val lastModified: String?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("primaryLanguage") val primaryLanguage: String?,
    @JsonProperty("copyrighted") val copyrighted: Boolean,
    @JsonProperty("includeShortName") val includeShortName: Boolean,
    @JsonProperty("includeNotes") val includeNotes: Boolean,
    @JsonProperty("contactPerson") val contactPerson: ContactPerson?,
    @JsonProperty("owningSection") val owningSection: String?,
    @JsonProperty("statisticalUnits") val statisticalUnits: List<String>?,
    @JsonProperty("versions") val versions: List<ClassificationVersion>?,
    @JsonProperty("_links") val links: ClassificationLinks?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class ContactPerson(
    @JsonProperty("name") val name: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("phone") val phone: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class ClassificationVersion(
    @JsonProperty("name") val name: String?,
    @JsonProperty("id") val id: Int?,
    @JsonProperty("validFrom") val validFrom: LocalDate?,
    @JsonProperty("lastModified") val lastModified: String?,
    @JsonProperty("published") val published: List<String>?,
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

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class KlassApiCodeListResponse(
    @JsonProperty("codes") val classificationItems: List<ClassificationItem>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class ClassificationItem(
    @JsonProperty("code") val code: String,
    @JsonProperty("parentCode") val parentCode: String?,
    @JsonProperty("level") val level: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("shortName") val shortName: String,
    @JsonProperty("presentationName") val presentationName: String,
    @JsonProperty("validFrom") val validFrom: LocalDate?,
    @JsonProperty("validTo") val validTo: LocalDate?,
    @JsonProperty("notes") val notes: String,
)

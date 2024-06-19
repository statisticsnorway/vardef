package no.ssb.metadata.vardef.integrations.klass.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Data classes for Klass Api response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class KlassApiResponse(
    @JsonProperty("_embedded") val embedded: Classifications,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Classifications(
    @JsonProperty("classifications") val classifications: List<Classification>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Classification(
    @JsonProperty("name") val name: String = "",
    @JsonProperty("id") val id: Int = 0,
    @JsonProperty("classificationType") val classificationType: String = "",
    @JsonProperty("lastModified") val lastModified: String = "",
    @JsonProperty("classificationItems") val classificationItems: List<ClassificationItem> = emptyList(),
    @JsonProperty("lastFetched") val lastFetched: LocalDateTime = LocalDateTime.now(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class KlassApiCodeListResponse(
    @JsonProperty("codes") val classificationItems: List<ClassificationItem>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class ClassificationItem(
    @JsonProperty("code") val code: String = "",
    @JsonProperty("name") val name: String = "",
    @JsonProperty("validFrom") val validFrom: LocalDate? = null,
    @JsonProperty("validTo") val validTo: LocalDate? = null,
    @JsonProperty("validFromInRequestedRange") val validFromInRequestedRange: LocalDate? = null,
    @JsonProperty("validToInRequestedRange") val validToInRequestedRange: LocalDate? = null,
)

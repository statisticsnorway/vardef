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
    val classifications: List<Classification>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Classification(
    val name: String = "",
    val id: Int = 0,
    val classificationType: String = "",
    val lastModified: String = "",
    val codes: List<Code> = emptyList(),
    val lastFetched: LocalDateTime = LocalDateTime.now(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class KlassApiCodeListResponse(
    @JsonProperty("codes") val codes: List<Code>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable
data class Code(
    val code: String = "",
    val name: String = "",
    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val validFromInRequestedRange: LocalDate? = null,
    val validToInRequestedRange: LocalDate? = null,
)

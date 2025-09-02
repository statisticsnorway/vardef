package no.ssb.metadata.vardef.integrations.dapla.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy

/**
 * Data classes for dapla-team-api response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable(naming = SnakeCaseStrategy::class)
data class Team(
    val uniformName: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@Serdeable(naming = SnakeCaseStrategy::class)
data class Group(
    val uniformName: String,
)

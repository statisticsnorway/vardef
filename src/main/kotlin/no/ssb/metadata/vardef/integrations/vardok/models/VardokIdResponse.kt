package no.ssb.metadata.vardef.integrations.vardok.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy

@Serdeable(naming = SnakeCaseStrategy::class)
data class VardokIdResponse(
    val vardokId: String,
)

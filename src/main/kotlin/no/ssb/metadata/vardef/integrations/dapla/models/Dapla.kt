package no.ssb.metadata.vardef.integrations.dapla.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Team(val uniformName: String)

@Serdeable
data class Group(val uniformName: String)

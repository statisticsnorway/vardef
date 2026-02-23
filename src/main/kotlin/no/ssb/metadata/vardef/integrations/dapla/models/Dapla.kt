package no.ssb.metadata.vardef.integrations.dapla.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class GraphQlResponse<T>(
    val data: T?,
)

@Serdeable
data class GraphQlRequest(
    val query: String,
    val variables: Map<String, Any?> = emptyMap(),
)

@Serdeable
data class GroupData(
    val group: Group?,
)

@Serdeable
data class Group(
    val name: String,
)

@Serdeable
data class TeamData(
    val team: Team?,
)

@Serdeable
data class Team(
    val slug: String,
    val section: Section?,
)

@Serdeable
data class Section(
    val code: String,
    val name: String?,
)

package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.PathVariable
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team

// @Client(id = "dapla-team-api")
@Headers(
    Header(name = USER_AGENT, value = "VarDef API"),
    Header(name = ACCEPT, value = "application/json"),
    Header(name = AUTHORIZATION),
)
interface DaplaTeamApiClient {
    @Get("/teams/{teamId}")
    @SingleResult
    fun fetchTeam(
        @PathVariable teamId: String,
    ): HttpResponse<Team?>

    @Get("/groups/{groupId}")
    @SingleResult
    fun fetchGroup(
        @PathVariable groupId: String,
    ): HttpResponse<Group?>

    @Get("teams/{teamId}/groups")
    @SingleResult
    fun fetchGroupsByTeam(
        @PathVariable teamId: String,
    ): HttpResponse<Map<String, List<Group>>?>
}

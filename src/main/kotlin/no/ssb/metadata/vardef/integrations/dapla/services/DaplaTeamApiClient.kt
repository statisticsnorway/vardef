package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team

/**
 * A declarative client for the Dapla Team API
 */
@Client(id = "dapla-team-api")
@Header(name = USER_AGENT, value = $$"${micronaut.http.request-headers.user-agent}")
@Header(name = ACCEPT, value = "application/hal+json")
interface DaplaTeamApiClient {
    @Get("/teams/{teamId}")
    @SingleResult
    fun fetchTeam(
        @PathVariable teamId: String,
        @Header("Authorization") authHeader: String,
    ): HttpResponse<Team?>

    @Get("/groups/{groupId}")
    @SingleResult
    fun fetchGroup(
        @PathVariable groupId: String,
        @Header("Authorization") authHeader: String,
    ): HttpResponse<Group?>
}

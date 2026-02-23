package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.dapla.models.GraphQlRequest
import no.ssb.metadata.vardef.integrations.dapla.models.GraphQlResponse
import no.ssb.metadata.vardef.integrations.dapla.models.GroupData
import no.ssb.metadata.vardef.integrations.dapla.models.TeamData

@Client(id = "dapla-api")
interface DaplaApiClient {
    @Post("/graphql", processes = [MediaType.APPLICATION_JSON])
    fun fetchTeam(
        @Body request: GraphQlRequest,
        @Header("Authorization") authHeader: String,
    ): HttpResponse<GraphQlResponse<TeamData>>

    @Post("/graphql", processes = [MediaType.APPLICATION_JSON])
    fun fetchGroup(
        @Body request: GraphQlRequest,
        @Header("Authorization") authHeader: String,
    ): HttpResponse<GraphQlResponse<GroupData>>
}

package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiCodeListResponse
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse

/**
 * A declarative client for the Klass API
 */
@Client(id = "klass")
@Headers(
    Header(name = USER_AGENT, value = "VarDef API"),
    Header(name = ACCEPT, value = "application/json"),
)
interface KlassApiClient {
    @Get("classifications?size=10000&language=nb&includeCodelists=true")
    @SingleResult
    @Consumes(MediaType.APPLICATION_JSON)
    fun fetchClassifications(): HttpResponse<KlassApiResponse>

    @Get("classifications/{classificationId}")
    @SingleResult
    @Consumes(MediaType.APPLICATION_JSON)
    fun fetchClassification(
        @PathVariable classificationId: Int,
    ): HttpResponse<Classification>

    @Get("classifications/{classificationId}/codesAt?date={codesAt}")
    @SingleResult
    @Consumes(MediaType.APPLICATION_JSON)
    fun fetchCodeList(
        @PathVariable classificationId: Int,
        @QueryValue codesAt: String,
    ): HttpResponse<KlassApiCodeListResponse>
}

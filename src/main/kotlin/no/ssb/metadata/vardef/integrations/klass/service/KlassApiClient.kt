package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiCodeListResponse
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse

/**
 * A declarative client response from Klass Api
 */
@Client(id = "klass")
@Headers(
    Header(name = USER_AGENT, value = "VarDef API"),
    Header(name = ACCEPT, value = "application/json"),
)
interface KlassApiClient {
    @Get("classifications?size=10000&language=nb&includeCodelists=true")
    @SingleResult
    fun fetchClassificationList(): KlassApiResponse?

    @Get("classifications/{classificationId}")
    @SingleResult
    fun fetchClassification(
        @PathVariable classificationId: Int,
    ): Classification?

    @Get("classifications/{classificationId}/codes?from=0000-01-01&to=9999-12-31")
    @SingleResult
    fun fetchCodeList(
        @PathVariable classificationId: Int,
    ): KlassApiCodeListResponse?
}

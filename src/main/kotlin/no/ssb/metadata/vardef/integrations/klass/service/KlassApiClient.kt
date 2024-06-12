package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Value
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiCodeListResponse
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import java.time.LocalDate

/**
 * A declarative client response from Klass Api
 */
@Client(id = "klass")
@Headers(
    Header(name = USER_AGENT, value = "VarDef API"),
    Header(name = ACCEPT, value = "application/json"),
)
interface KlassApiClient {
    @Get("classifications")
    @SingleResult
    fun fetchClassificationList(): KlassApiResponse

    @Get("classifications/{classificationId}")
    @SingleResult
    fun fetchClassification(@PathVariable classificationId: Int): Classification?

    @Get("classifications/{classificationId}/codesAt?date={date}")
    @SingleResult
    fun fetchCodeListAtDate(@PathVariable classificationId: Int, @QueryValue date: LocalDate): KlassApiCodeListResponse?
}

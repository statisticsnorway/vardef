package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.client.annotation.Client
import io.netty.handler.codec.http.HttpResponse
import no.ssb.metadata.vardef.integrations.klass.models.Response


/**
 * A declarative client
 */
@Client("https://data.ssb.no/api/klass/v1/classifications")
@Headers(
    Header(name = USER_AGENT, value = "Micronaut HTTP Client"),
    Header(name = ACCEPT, value = "application/json"),
)
interface KlassApiClient {
    @Get()
    @SingleResult
    fun fetchClassifications(): HttpResponse
}

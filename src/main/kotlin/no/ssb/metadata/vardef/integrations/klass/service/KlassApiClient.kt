package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse

/**
 * A declarative client response from Klass Api
 */
@Client(id = "klass")
@Headers(
    Header(name = ACCEPT, value = "application/json"),
)
interface KlassApiClient {
    @Get("classifications")
    @SingleResult
    fun fetchClassificationList(): KlassApiResponse
}

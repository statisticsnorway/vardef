package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Headers
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.reactivestreams.Publisher

/**
 * A declarative client
 */
@Client(id = "klass.url")
@Headers(
    Header(name = USER_AGENT, value = "Micronaut HTTP Client"),
    Header(name = ACCEPT, value = "application/vnd.github.v3+json, application/json"),
)
interface KlassApiClient {
    @Get("/classifications")
    @SingleResult
    fun fetchClassifications(): Publisher<List<ClassificationItem>>
}

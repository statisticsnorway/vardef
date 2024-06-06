package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.uri.UriBuilder
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.reactivestreams.Publisher
import java.net.URI

@Singleton
class Client(
    @param:Client(id = "klass.url") private val httpClient: HttpClient,
) {
    private val uri: URI =
        UriBuilder.of("/classifications")
            .path("classifications")
            .build()

    fun fetchClassifications(): Publisher<List<ClassificationItem>> {
        val req: HttpRequest<*> =
            HttpRequest.GET<Any>(uri)
                .header(USER_AGENT, "Micronaut HTTP Client")
                .header(ACCEPT, "application/json")
        return httpClient.retrieve(req, Argument.listOf(ClassificationItem::class.java))
    }
}

package no.ssb.metadata.vardef.integrations.klass.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.MediaType
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn


@Controller("/api")
@ExecuteOn(TaskExecutors.BLOCKING)
class MyController(@Client("/") private val client: HttpClient) {

    @Get("/fetch")
    @Produces(MediaType.APPLICATION_JSON)
    fun fetchResponse(): HttpResponse<String> {
        val response = client.toBlocking().exchange("https://data.ssb.no/api/klass/v1/classifications", String::class.java)
        return HttpResponse.ok(response.body())
    }
}

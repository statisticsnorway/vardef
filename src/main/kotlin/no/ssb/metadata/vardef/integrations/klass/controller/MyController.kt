package no.ssb.metadata.vardef.integrations.klass.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.MediaType
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import no.ssb.metadata.vardef.integrations.klass.models.ClassResult
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import io.micronaut.serde.ObjectMapper


@Controller("/api")
@ExecuteOn(TaskExecutors.BLOCKING)
class MyController(@Client("/") private val client: HttpClient) {

    @Get("/fetch")
    @Produces(MediaType.APPLICATION_JSON)
    fun fetchResponse(): HttpResponse<String> {
        val response = client.toBlocking().exchange("https://data.ssb.no/api/klass/v1/classifications", String::class.java)
        val responseList = response.body()
        return HttpResponse.ok(responseList)
    }

    @Get("/fetch-list")
    fun fetchList(): HttpResponse<List<Classification>> {
        return try {
            // Make the HTTP request and deserialize the response into a ClassificationResponse object
            val response = client.toBlocking().exchange("https://data.ssb.no/api/klass/v1/classifications", ClassResult::class.java)
            val classificationResponse = response.body()

            // Extract the classifications list from the response
            val classifications = classificationResponse?.embedded?.classifications ?: emptyList()

            // Return an HttpResponse containing the classifications
            HttpResponse.ok(classifications)
        } catch (e: HttpStatusException) {
            // Return an HttpResponse with the error status and message
            HttpResponse.status<List<Classification>>(e.status).body(emptyList())
        } catch (e: Exception) {
            // Return a server error HttpResponse with the exception message
            HttpResponse.serverError<List<Classification>>().body(emptyList())
        }
    }
}

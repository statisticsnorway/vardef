package no.ssb.metadata.vardef.integrations.klass.controller

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.service.Client
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.reactivestreams.Publisher

@Controller()
@ExecuteOn(TaskExecutors.BLOCKING)
class ClassificationController(private val client: Client, val klassApiClient: KlassApiClient) {
    @Get("/classifications-lowlevel")
    @SingleResult
    fun classificationsWithLowLevelClient(): Publisher<List<ClassificationItem>> {
        return client.fetchClassifications()
    }

    /*
     @SingleResult
    fun fetchClassifications(): Publisher<ClassificationItem> {
        return klassApiClient.fetchClassifications()
    }
     */
    @Get("/classifications")
    @SingleResult
    fun fetchClassifications(): KlassApiResponse {
        return klassApiClient.fetchClassifications()
    }
}

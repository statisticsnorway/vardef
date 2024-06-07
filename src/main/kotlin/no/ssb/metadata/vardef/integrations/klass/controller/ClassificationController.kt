package no.ssb.metadata.vardef.integrations.klass.controller

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import io.micronaut.http.HttpResponse
import org.slf4j.LoggerFactory

@Controller()
@ExecuteOn(TaskExecutors.BLOCKING)
class ClassificationController {

    @Inject
    lateinit var klassApiClient: KlassApiClient

    @Get("/classifications")
    @SingleResult
    fun fetchClassifications(): HttpResponse<KlassApiResponse> {
        return try {
            HttpResponse.ok(klassApiClient.fetchClassificationList())
        }
        catch (e: Exception) {
            HttpResponse.notFound()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ClassificationController::class.java)
    }
}

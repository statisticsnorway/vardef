package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.uri.UriBuilder
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.swagger.v3.oas.annotations.Hidden
import java.net.URI

/**
 * Home controller
 *
 * Redirect GET requests to the root path to the API docs. This makes the API docs more discoverable from a browser.
 */
@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
class HomeController {
    private val docsUri: URI = UriBuilder.of("/docs").path("redoc").build()

    @Get
    @Hidden
    fun redirectToDocs(): HttpResponse<Any> = HttpResponse.seeOther(docsUri)
}

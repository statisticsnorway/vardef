package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders.AUTHORIZATION
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MutableHttpHeaders
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.client.ProxyHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.ACTIVE_TEAM
import no.ssb.metadata.vardef.constants.DATA_MIGRATION
import no.ssb.metadata.vardef.constants.DRAFT_EXAMPLE
import no.ssb.metadata.vardef.integrations.vardok.models.VardokNotFoundException
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import org.reactivestreams.Publisher

@Tag(name = DATA_MIGRATION)
@Validated
@Controller("/vardok-migration/{vardok-id}")
@ExecuteOn(TaskExecutors.BLOCKING)
class VarDokMigrationController {
    @Inject
    lateinit var vardokService: VardokService

    @Client("/")
    @Inject
    lateinit var httpClient: ProxyHttpClient

    /**
     * Create a variable definition from a VarDok variable definition.
     */
    @Post
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created.",
        content =
            [
                Content(
                    examples = [
                        ExampleObject(
                            DRAFT_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    @ApiResponse(responseCode = "400", description = "The definition in Vardok has missing or malformed metadata.")
    fun createVariableDefinitionFromVarDok(
        @Parameter(name = "vardok-id", description = "The ID of the definition in Vardok.", example = "1607")
        @PathVariable("vardok-id")
        id: String,
        httpRequest: HttpRequest<*>,
    ): Publisher<MutableHttpResponse<*>>? {
        try {
            val varDefInput =
                vardokService.createVarDefInputFromVarDokItems(
                    vardokService.fetchMultipleVardokItemsByLanguage(id),
                )

            // Get token from header
            val authHeader = httpRequest.headers.get(AUTHORIZATION)

            val selectedGroup = httpRequest.parameters.get(ACTIVE_GROUP)
            // val selectedTeam = httpRequest.parameters.get(ACTIVE_TEAM)

            return httpClient.proxy(
                HttpRequest.POST("/variable-definitions?$ACTIVE_GROUP=$selectedGroup", varDefInput).headers {
                        entries: MutableHttpHeaders ->
                    authHeader?.let {
                        // Set authorization header for post to /variable-definitions
                        entries.set(AUTHORIZATION, it)
                    }
                },
            )
        } catch (e: VardokNotFoundException) {
            // We always want to return NOT_FOUND in this case
            throw HttpStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: Exception) {
            // Other validation exceptions
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}

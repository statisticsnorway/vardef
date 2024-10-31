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
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.vardok.models.VardokNotFoundException
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_CREATOR
import org.reactivestreams.Publisher

@Tag(name = DATA_MIGRATION)
@Validated
@Controller("/vardok-migration/{vardok-id}")
@Secured(VARIABLE_CREATOR)
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
                    schema = Schema(implementation = CompleteResponse::class),
                    examples = [
                        ExampleObject(
                            name = "migrate_1607",
                            value = COMPLETE_RESPONSE_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    @ApiResponse(responseCode = "400", description = "The definition in Vardok has missing or malformed metadata.")
    @SecurityRequirement(name = "Bearer Authentication")
    fun createVariableDefinitionFromVarDok(
        @Parameter(
            name = "vardok-id",
            description = "The ID of the definition in Vardok.",
            examples = [
                ExampleObject(
                    name = "migrate_1607",
                    value = "1607",
                ),
            ],
        )
        @PathVariable("vardok-id")
        id: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "migrate_1607",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        httpRequest: HttpRequest<*>,
    ): Publisher<MutableHttpResponse<*>>? {
        try {
            val varDefInput =
                vardokService.createVarDefInputFromVarDokItems(
                    vardokService.fetchMultipleVardokItemsByLanguage(id),
                )

            return httpClient.proxy(
                HttpRequest
                    .POST("/variable-definitions?$ACTIVE_GROUP=$activeGroup", varDefInput)
                    .headers { entries: MutableHttpHeaders ->
                        entries.set(AUTHORIZATION, httpRequest.headers.get(AUTHORIZATION))
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

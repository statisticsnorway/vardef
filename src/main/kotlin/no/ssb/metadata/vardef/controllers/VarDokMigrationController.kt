package no.ssb.metadata.vardef.controllers

import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.HttpClient.DEFAULT_ERROR_TYPE
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
import no.ssb.metadata.vardef.constants.DATA_MIGRATION
import no.ssb.metadata.vardef.constants.DRAFT_EXAMPLE
import no.ssb.metadata.vardef.integrations.vardok.VarDokService
import no.ssb.metadata.vardef.integrations.vardok.VardokNotFoundException
import no.ssb.metadata.vardef.models.Draft
import org.reactivestreams.Publisher

@Tag(name = DATA_MIGRATION)
@Validated
@Controller("/vardok-migration/{vardok-id}")
@ExecuteOn(TaskExecutors.BLOCKING)
class VarDokMigrationController {
    @Inject
    lateinit var varDokApiService: VarDokService

    @Client("/")
    @Inject
    lateinit var httpClient: HttpClient

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
    ): Publisher<HttpResponse<Draft>>? {
        try {
            val varDefInput =
                varDokApiService.createVarDefInputFromVarDokItems(
                    varDokApiService.fetchMultipleVarDokItemsByLanguage(id),
                )
            return httpClient.exchange(
                HttpRequest.POST("/variable-definitions", varDefInput),
                Argument.of(Draft::class.java),
                DEFAULT_ERROR_TYPE
            )
        } catch (e: VardokNotFoundException) {
            throw HttpStatusException(HttpStatus.NOT_FOUND, e.message)
        } catch (e: Exception) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}

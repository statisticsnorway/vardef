package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.micronaut.http.client.HttpClient
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
import no.ssb.metadata.vardef.constants.INPUT_VARIABLE_DEFINITION_EXAMPLE
import no.ssb.metadata.vardef.integrations.vardok.VarDokService
import no.ssb.metadata.vardef.integrations.vardok.VardokException
import no.ssb.metadata.vardef.models.InputVariableDefinition

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
                            INPUT_VARIABLE_DEFINITION_EXAMPLE,
                        ),
                    ],
                ),
            ],
    )
    @ApiResponse(responseCode = "400", description = "The definition in Vardok has missing or malformed metadata.")
    fun createVariableDefinitionFromVarDok(
        @Parameter(name = "The ID of the definition in Vardok.", example = "1607") @PathVariable("vardok-id") id: String,
    ): InputVariableDefinition? {
        try {
            val varDefInput =
                varDokApiService.createVarDefInputFromVarDokItems(
                    varDokApiService.fetchMultipleVarDokItemsByLanguage(id),
                )
            return httpClient.toBlocking().retrieve(
                HttpRequest.POST("/variable-definitions", varDefInput),
                InputVariableDefinition::class.java,
            )
        } catch (e: VardokException) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}

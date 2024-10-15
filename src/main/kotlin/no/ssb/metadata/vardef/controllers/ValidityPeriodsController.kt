package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
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
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.exceptions.ValidityPeriodExceptions
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.ValidityPeriodsService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId

@Tag(name = VALIDITY_PERIODS)
@Validated
@Controller("/variable-definitions/{variable-definition-id}/validity-periods")
@ExecuteOn(TaskExecutors.BLOCKING)
class ValidityPeriodsController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Inject
    lateinit var validityPeriods: ValidityPeriodsService

    @Inject
    lateinit var patches: PatchesService

    /**
     * Create a new validity period for a variable definition.
     */
    @Post
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created.",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "create_validity_period",
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "400", description = "The request is missing or has errors in required fields.")
    @ApiResponse(responseCode = "405", description = "Method only allowed for published variables.")
    fun createValidityPeriod(
        @PathVariable("variable-definition-id")
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "create_validity_period", value = ID_EXAMPLE)])
        @VardefId
        variableDefinitionId: String,
        @Body
        @Valid
        @Parameter(examples = [ExampleObject(name = "create_validity_period", value = VALIDITY_PERIOD_EXAMPLE)])
        newPeriod: ValidityPeriod,
    ): CompleteResponse {
        val latestExistingPatch = patches.latest(variableDefinitionId)

        if (!latestExistingPatch.variableStatus.isPublished()) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only allowed for published variables.")
        }

        return try {
            validityPeriods.saveNewValidityPeriod(newPeriod, variableDefinitionId).toCompleteResponse()
        } catch (e: ValidityPeriodExceptions) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    /**
     * List all validity periods.
     *
     * This is rendered in the given language, with the default being Norwegian Bokmål.
     */
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = VALIDITY_PERIODS)
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "???",
                        value = LIST_OF_RENDERED_VARIABLE_DEFINITIONS_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun listValidityPeriods(
        @PathVariable("variable-definition-id")
        variableDefinitionId: String,
        @Header("Accept-Language", defaultValue = DEFAULT_LANGUAGE)
        language: SupportedLanguages,
    ): List<RenderedVariableDefinition> = validityPeriods.listValidityPeriodsById(language, variableDefinitionId)
}

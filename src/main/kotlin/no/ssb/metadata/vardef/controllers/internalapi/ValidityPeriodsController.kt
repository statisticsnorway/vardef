package no.ssb.metadata.vardef.controllers.internalapi

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.ssb.metadata.vardef.annotations.BadRequestApiResponse
import no.ssb.metadata.vardef.annotations.MethodNotAllowedApiResponse
import no.ssb.metadata.vardef.annotations.NotFoundApiResponse
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_OWNER
import no.ssb.metadata.vardef.services.ValidityPeriodsService

@Tag(name = VALIDITY_PERIODS)
@Validated
@Controller("/variable-definitions/{$VARIABLE_DEFINITION_ID_PATH_VARIABLE}/validity-periods")
@ExecuteOn(TaskExecutors.BLOCKING)
@Secured(VARIABLE_CONSUMER)
@SecurityRequirement(name = LABID_TOKEN_SCHEME)
class ValidityPeriodsController(
    private val validityPeriods: ValidityPeriodsService,
) {
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
                        name = "Create validity period",
                        value = COMPLETE_VIEW_EXAMPLE,
                    ),
                ],
                schema = Schema(implementation = CompleteView::class),
            ),
        ],
    )
    @NotFoundApiResponse
    @BadRequestApiResponse
    @MethodNotAllowedApiResponse
    @Secured(VARIABLE_OWNER)
    fun createValidityPeriod(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Create validity period", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        variableDefinitionId: String,
        @Body
        @Valid
        @RequestBody(
            content = [
                Content(
                    examples = [ExampleObject(name = "Create validity period", value = VALIDITY_PERIOD_EXAMPLE)],
                    schema = Schema(implementation = ValidityPeriod::class),
                ),
            ],
        )
        newPeriod: ValidityPeriod,
        authentication: Authentication,
    ): CompleteView {
        val latestExistingPatch = validityPeriods.getLatestPatchInLastValidityPeriod(variableDefinitionId)

        if (!latestExistingPatch.variableStatus.isPublished()) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only allowed for published variables.")
        }

        return validityPeriods.create(variableDefinitionId, newPeriod, authentication.name).toCompleteView()
    }

    /**
     * List all validity periods.
     */
    @Get
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Validity periods",
                        value = LIST_OF_COMPLETE_VIEW_EXAMPLE,
                    ),
                ],
                array = ArraySchema(schema = Schema(implementation = CompleteView::class)),
            ),
        ],
    )
    @NotFoundApiResponse
    fun listValidityPeriods(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Validity periods", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        variableDefinitionId: String,
    ): List<CompleteView> = validityPeriods.listComplete(variableDefinitionId)
}

package no.ssb.metadata.vardef.controllers.internalapi

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.json.JsonMapper
import io.micronaut.json.tree.JsonNode
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
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import no.ssb.metadata.vardef.annotations.BadRequestApiResponse
import no.ssb.metadata.vardef.annotations.MethodNotAllowedApiResponse
import no.ssb.metadata.vardef.annotations.NotFoundApiResponse
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.CreateValidityPeriod
import no.ssb.metadata.vardef.models.CreateValidityPeriodInput
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.security.Roles
import no.ssb.metadata.vardef.services.ValidityPeriodsService

@Tag(name = VALIDITY_PERIODS)
@Validated
@Controller("/variable-definitions/{$VARIABLE_DEFINITION_ID_PATH_VARIABLE}/validity-periods")
@ExecuteOn(TaskExecutors.BLOCKING)
@Secured(Roles.VARIABLE_CONSUMER)
class ValidityPeriodsController(
    private val validityPeriods: ValidityPeriodsService,
    private val jsonMapper: JsonMapper,
    private val validator: Validator,
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
    @Secured(Roles.VARIABLE_OWNER)
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
        @RequestBody(
            content = [
                Content(
                    examples = [ExampleObject(name = "Create validity period", value = CREATE_VALIDITY_PERIOD_EXAMPLE)],
                    schema = Schema(implementation = CreateValidityPeriod::class),
                ),
            ],
        )
        body: JsonNode,
        authentication: Authentication,
    ): CompleteView {
        val newPeriodInput =
            try {
                CreateValidityPeriodInput.fromJson(body, jsonMapper)
            } catch (e: IllegalArgumentException) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message ?: "")
            }

        val newPeriod =
            try {
                newPeriodInput.toCreateValidityPeriod()
            } catch (e: IllegalArgumentException) {
                throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message ?: "")
            }

        val violations = validator.validate(newPeriod)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }

        val latestExistingPatch = validityPeriods.getLatestPatchInLastValidityPeriod(variableDefinitionId)

        if (!latestExistingPatch.variableStatus.isPublished()) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only allowed for published variables.")
        }

        return validityPeriods.create(variableDefinitionId, newPeriodInput, authentication.name).toCompleteView()
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

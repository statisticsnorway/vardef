package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
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
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.security.VARIABLE_OWNER
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.ValidityPeriodsService
import no.ssb.metadata.vardef.validators.VardefId

@Tag(name = VALIDITY_PERIODS)
@Validated
@Controller("/variable-definitions/{$VARIABLE_DEFINITION_ID_PATH_VARIABLE}/validity-periods")
@ExecuteOn(TaskExecutors.BLOCKING)
class ValidityPeriodsController {
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
    @Secured(VARIABLE_OWNER)
    fun createValidityPeriod(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "create_validity_period", value = ID_EXAMPLE)])
        @VardefId
        variableDefinitionId: String,
        @Body
        @Valid
        @Parameter(examples = [ExampleObject(name = "create_validity_period", value = VALIDITY_PERIOD_EXAMPLE)])
        newPeriod: ValidityPeriod,
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
    ): CompleteResponse {
        val latestExistingPatch = validityPeriods.getLatestPatchInLastValidityPeriod(variableDefinitionId)

        val savedGroup = latestExistingPatch.owner
        if (!patches.isValidGroup(activeGroup, savedGroup)) {
            throw HttpStatusException(
                HttpStatus.FORBIDDEN,
                "Only members of the groups ${savedGroup.groups} are allowed to edit this variable",
            )
        }

        if (!latestExistingPatch.variableStatus.isPublished()) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only allowed for published variables.")
        }

        return try {
            validityPeriods.create(variableDefinitionId, newPeriod).toCompleteResponse()
        } catch (e: ValidityPeriodExceptions) {
            throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
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
                        name = "one_validity_period",
                        value = LIST_OF_COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun listValidityPeriods(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "one_validity_period", value = ID_EXAMPLE)])
        @VardefId
        variableDefinitionId: String,
    ): List<CompleteResponse> = validityPeriods.listComplete(variableDefinitionId)
}

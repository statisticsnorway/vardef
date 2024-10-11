package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
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
import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException
import no.ssb.metadata.vardef.exceptions.PublishedVariableAccessException
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId

@Tag(name = VALIDITY_PERIODS)
@Validated
@Controller("/variable-definitions/{variable-definition-id}/validity-periods")
@ExecuteOn(TaskExecutors.BLOCKING)
class ValidityPeriodsController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

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
    @ApiResponse(responseCode = "400", description = "Bad request.")
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
        val latestExistingPatch = varDefService.getLatestPatchById(variableDefinitionId)
        when {
            !latestExistingPatch.variableStatus.isPublished() ->
                throw PublishedVariableAccessException()

            !varDefService.isValidValidFromValue(variableDefinitionId, newPeriod.validFrom) ->
                throw InvalidValidFromException()

            !varDefService.isNewDefinition(variableDefinitionId, newPeriod) ->
                throw DefinitionTextUnchangedException()
        }
        return varDefService.saveNewValidityPeriod(newPeriod, variableDefinitionId).toCompleteResponse()
    }
}

package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.ID_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.VALIDITY_PERIODS
import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException
import no.ssb.metadata.vardef.exceptions.PublishedVariableAccessException
import no.ssb.metadata.vardef.models.FullResponseVariableDefinition
import no.ssb.metadata.vardef.models.InputVariableDefinition
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
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "405", description = "Method only allowed for published variables.")
    fun createValidityPeriod(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
        @Body @Valid newPeriod: InputVariableDefinition,
    ): FullResponseVariableDefinition {
        val latestExistingPatch = varDefService.getLatestPatchById(variableDefinitionId)
        when {
            !latestExistingPatch.variableStatus.isPublished() ->
                throw PublishedVariableAccessException()

            !varDefService.isValidValidFromValue(variableDefinitionId, newPeriod.validFrom) ->
                throw InvalidValidFromException()

            !varDefService.isNewDefinition(newPeriod, latestExistingPatch) ->
                throw DefinitionTextUnchangedException()
        }
        return varDefService.saveNewValidityPeriod(newPeriod, variableDefinitionId).toFullResponseVariableDefinition()
    }
}

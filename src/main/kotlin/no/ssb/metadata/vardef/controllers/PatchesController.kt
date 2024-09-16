package no.ssb.metadata.vardef.controllers

import io.micronaut.http.*
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
import no.ssb.metadata.vardef.exceptions.PublishedVariableAccessException
import no.ssb.metadata.vardef.models.FullResponseVariableDefinition
import no.ssb.metadata.vardef.models.InputPatchVariableDefinition
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId

@Tag(name = "Patches")
@Validated
@Controller("/variable-definitions/{variable-definition-id}/patches")
@ExecuteOn(TaskExecutors.BLOCKING)
class PatchesController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    /**
     * List all patches for the given variable definition.
     *
     * The full object is returned for comparison purposes.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get
    fun getAllPatches(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
    ): MutableHttpResponse<List<FullResponseVariableDefinition>> =
        HttpResponse
            .ok(
                varDefService
                    .listAllPatchesById(id = variableDefinitionId)
                    .map { it.toFullResponseVariableDefinition() },
            ).contentType(MediaType.APPLICATION_JSON)

    /**
     * Get one concrete patch for the given variable definition.
     *
     * The full object is returned for comparison purposes.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get("/{patch-id}")
    fun getOnePatch(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
        @PathVariable("patch-id") patchId: Int,
    ): MutableHttpResponse<FullResponseVariableDefinition> =
        HttpResponse
            .ok(
                varDefService
                    .getOnePatchById(variableDefinitionId, patchId = patchId)
                    .toFullResponseVariableDefinition(),
            ).contentType(MediaType.APPLICATION_JSON)

    /**
     * Create a new patch for a variable definition.
     *
     */
    @Post
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "405", description = "Method not allowed.")
    fun createPatch(
        @PathVariable("variable-definition-id") @Schema(description = ID_FIELD_DESCRIPTION) @VardefId variableDefinitionId: String,
        @Body @Valid patch: InputPatchVariableDefinition,
    ): FullResponseVariableDefinition {
        // TODO validate content of the new patch
        val latestExistingPatch = varDefService.getLatestPatchById(variableDefinitionId)
        if (!latestExistingPatch.variableStatus.isPublished()) {
            throw PublishedVariableAccessException()
        }

        return varDefService.save(
            patch.toSavedVariableDefinition(latestExistingPatch, latestExistingPatch.patchId),
        ).toFullResponseVariableDefinition()
    }
}

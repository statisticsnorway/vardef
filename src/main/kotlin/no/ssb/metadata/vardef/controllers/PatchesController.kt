package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.exceptions.PublishedVariableAccessException
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId

@Tag(name = PATCHES)
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
    @ApiResponse(
        responseCode = "200",
        content = [
            io.swagger.v3.oas.annotations.media.Content(
                examples = [
                    io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "one_patch",
                        value = """[$FULL_RESPONSE_VARIABLE_DEFINITION_EXAMPLE]""",
                    ),
                ],
            ),
        ],
    )
    @Get
    fun getAllPatches(
        @PathVariable("variable-definition-id")
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "one_patch", value = ID_EXAMPLE)])
        @VardefId
        variableDefinitionId: String,
    ): MutableHttpResponse<List<CompleteResponse>> =
        HttpResponse
            .ok(
                varDefService
                    .listAllPatchesById(id = variableDefinitionId)
                    .map { it.toCompleteResponse() },
            ).contentType(MediaType.APPLICATION_JSON)

    /**
     * Get one concrete patch for the given variable definition.
     *
     * The full object is returned for comparison purposes.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(
        responseCode = "200",
        content = [
            io.swagger.v3.oas.annotations.media.Content(
                examples = [
                    io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "patch_1",
                        value = FULL_RESPONSE_VARIABLE_DEFINITION_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get("/{patch-id}")
    fun getOnePatch(
        @PathVariable("variable-definition-id")
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "patch_1", value = ID_EXAMPLE)])
        @VardefId
        variableDefinitionId: String,
        @PathVariable("patch-id")
        @Parameter(description = "ID of the patch to retrieve", examples = [ExampleObject(name = "patch_1", value = "1")])
        patchId: Int,
    ): MutableHttpResponse<CompleteResponse> =
        HttpResponse
            .ok(
                varDefService
                    .getOnePatchById(variableDefinitionId, patchId = patchId)
                    .toCompleteResponse(),
            ).contentType(MediaType.APPLICATION_JSON)

    /**
     * Create a new patch for a variable definition.
     *
     */
    @Post
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created.",
        content = [
            io.swagger.v3.oas.annotations.media.Content(
                examples = [
                    io.swagger.v3.oas.annotations.media.ExampleObject(
                        name = "create_patch",
                        value = FULL_RESPONSE_VARIABLE_DEFINITION_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "405", description = "Method only allowed for published variables.")
    fun createPatch(
        @PathVariable("variable-definition-id")
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "create_patch", value = ID_EXAMPLE)])
        @VardefId
        variableDefinitionId: String,
        @Parameter(examples = [ExampleObject(name = "create_patch", value = PATCH_EXAMPLE)])
        @Body
        @Valid
        patch: Patch,
    ): CompleteResponse {
        val latestExistingPatch = varDefService.getLatestPatchById(variableDefinitionId)

        if (!latestExistingPatch.variableStatus.isPublished()) {
            throw PublishedVariableAccessException()
        }

        return varDefService
            .save(
                patch.toSavedVariableDefinition(latestExistingPatch),
            ).toCompleteResponse()
    }
}

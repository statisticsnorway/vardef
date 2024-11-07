package no.ssb.metadata.vardef.controllers

import io.micronaut.core.convert.format.Format
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_OWNER
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.ValidityPeriodsService
import java.time.LocalDate

@Tag(name = PATCHES)
@Validated
@Controller("/variable-definitions/{$VARIABLE_DEFINITION_ID_PATH_VARIABLE}/patches")
@Secured(VARIABLE_CONSUMER)
@ExecuteOn(TaskExecutors.BLOCKING)
class PatchesController {
    @Inject
    lateinit var validityPeriods: ValidityPeriodsService

    @Inject
    lateinit var patches: PatchesService

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
                        value = """[$COMPLETE_RESPONSE_EXAMPLE]""",
                    ),
                ],
            ),
        ],
    )
    @Get
    @SecurityRequirement(name = "Bearer Authentication")
    fun getAllPatches(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "one_patch", value = ID_EXAMPLE)])
        variableDefinitionId: String,
    ): List<CompleteResponse> =
        patches
            .list(definitionId = variableDefinitionId)
            .map { it.toCompleteResponse() }

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
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get("/{patch-id}")
    @SecurityRequirement(name = "Bearer Authentication")
    fun getOnePatch(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "patch_1", value = ID_EXAMPLE)])
        variableDefinitionId: String,
        @PathVariable("patch-id")
        @Parameter(description = "ID of the patch to retrieve", examples = [ExampleObject(name = "patch_1", value = "1")])
        patchId: Int,
    ): CompleteResponse =
        patches
            .get(variableDefinitionId, patchId = patchId)
            .toCompleteResponse()

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
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "405", description = "Method only allowed for published variables.")
    @Secured(VARIABLE_OWNER)
    @SecurityRequirement(name = "Bearer Authentication")
    fun createPatch(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "create_patch", value = ID_EXAMPLE)])
        variableDefinitionId: String,
        @QueryValue("valid_from")
        @Parameter(
            description = VALID_FROM_QUERY_PARAMETER_DESCRIPTION,
            examples = [ExampleObject(name = "create_patch", value = DATE_EXAMPLE)],
        )
        @Format(DATE_FORMAT)
        validFrom: LocalDate?,
        @Parameter(examples = [ExampleObject(name = "create_patch", value = PATCH_EXAMPLE)])
        @Body
        @Valid
        patch: Patch,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "create_patch",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
    ): CompleteResponse {
        val latestPatchOnValidityPeriod = validityPeriods.getMatchingOrLatest(variableDefinitionId, validFrom)

        if (!latestPatchOnValidityPeriod.variableStatus.isPublished()) {
            throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only allowed for published variables.")
        }
        return patches
            .create(
                patch,
                variableDefinitionId,
                latestPatchOnValidityPeriod,
            ).toCompleteResponse()
    }
}

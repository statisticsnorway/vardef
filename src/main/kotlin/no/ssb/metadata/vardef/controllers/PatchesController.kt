package no.ssb.metadata.vardef.controllers

import io.micronaut.core.convert.format.Format
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
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
@SecurityRequirement(name = KEYCLOAK_TOKEN_SCHEME)
@ExecuteOn(TaskExecutors.BLOCKING)
class PatchesController(
    private val validityPeriods: ValidityPeriodsService,
    private val patches: PatchesService,
) {
    /**
     * List all patches for the given variable definition.
     *
     * The full object is returned for comparison purposes.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @NotFoundApiResponse
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Patches",
                        value = LIST_OF_COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
                array = ArraySchema(schema = Schema(implementation = CompleteResponse::class)),
            ),
        ],
    )
    @Get
    fun listPatches(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Patches", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
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
                    ExampleObject(
                        name = "Patch",
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
                schema = Schema(implementation = CompleteResponse::class),
            ),
        ],
    )
    @NotFoundApiResponse
    @Get("/{patch-id}")
    fun getPatch(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Patch", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_EXAMPLE),
            ],
        )
        variableDefinitionId: String,
        @PathVariable("patch-id")
        @Parameter(
            description = "ID of the patch to retrieve",
            examples = [
                ExampleObject(name = "Patch", value = "1"),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = "244"),
            ],
        )
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
            Content(
                examples = [
                    ExampleObject(
                        name = "Create patch",
                        value = COMPLETE_RESPONSE_EXAMPLE_PUBLISHED_VARIABLE,
                    ),
                ],
                schema = Schema(implementation = CompleteResponse::class),
            ),
        ],
    )
    @NotFoundApiResponse
    @BadRequestApiResponse
    @MethodNotAllowedApiResponse
    @Secured(VARIABLE_OWNER)
    fun createPatch(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Create patch", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        variableDefinitionId: String,
        @QueryValue("valid_from")
        @Parameter(
            description = VALID_FROM_QUERY_PARAMETER_DESCRIPTION,
            examples = [ExampleObject(name = "Create patch", value = DATE_EXAMPLE)],
        )
        @Format(DATE_FORMAT)
        validFrom: LocalDate?,
        @RequestBody(
            content = [
                Content(
                    examples = [ExampleObject(name = "Create patch", value = PATCH_EXAMPLE)],
                    schema = Schema(implementation = Patch::class),
                ),
            ],
        )
        @Body
        @Valid
        patch: Patch,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Create patch",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        authentication: Authentication,
    ): CompleteResponse {
        val latestPatchOnValidityPeriod = validityPeriods.getMatchingOrLatest(variableDefinitionId, validFrom)
        when {
            !latestPatchOnValidityPeriod.variableStatus.isPublished() ->
                throw HttpStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only allowed for published variables.")
        }
        return patches
            .create(
                patch,
                variableDefinitionId,
                latestPatchOnValidityPeriod,
                authentication.name,
            ).toCompleteResponse()
    }
}

package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.ssb.metadata.vardef.annotations.BadRequestApiResponse
import no.ssb.metadata.vardef.annotations.ConflictApiResponse
import no.ssb.metadata.vardef.annotations.MethodNotAllowedApiResponse
import no.ssb.metadata.vardef.annotations.NotFoundApiResponse
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.UpdateDraft
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.models.isPublished
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_OWNER
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import java.time.LocalDate

@Validated
@Controller("/variable-definitions/{$VARIABLE_DEFINITION_ID_PATH_VARIABLE}")
@Secured(VARIABLE_CONSUMER)
@SecurityRequirement(name = KEYCLOAK_TOKEN_SCHEME)
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionByIdController(
    private val vardef: VariableDefinitionService,
    private val patches: PatchesService,
) {
    /**
     * Get one variable definition.
     */
    @Tag(name = VARIABLE_DEFINITIONS)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [
                    ExampleObject(name = "Date not specified", value = COMPLETE_RESPONSE_EXAMPLE),
                    ExampleObject(name = "Specific date", value = COMPLETE_RESPONSE_EXAMPLE),
                ],
                schema = Schema(implementation = CompleteResponse::class),
            ),
        ],
    )
    @NotFoundApiResponse
    @Get
    fun getVariableDefinitionById(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Date not specified", value = ID_EXAMPLE),
                ExampleObject(name = "Specific date", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        definitionId: String,
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(name = "Specific date", value = DATE_EXAMPLE),
            ],
        )
        @QueryValue("date_of_validity")
        dateOfValidity: LocalDate? = null,
    ): CompleteResponse =
        vardef
            .getCompleteByDate(
                definitionId = definitionId,
                dateOfValidity = dateOfValidity,
            )
            ?: throw HttpStatusException(
                HttpStatus.NOT_FOUND,
                "Variable with ID $definitionId not found${if (dateOfValidity == null) "" else " for date $dateOfValidity"}",
            )

    /**
     * Delete a variable definition.
     */
    @Tag(name = DRAFT)
    @ApiResponse(
        responseCode = "204",
        description = "Successfully deleted",
        content = [
            Content(
                examples = [
                    ExampleObject(name = "Delete", value = ""),
                ],
            ),
        ],
    )
    @NotFoundApiResponse
    @MethodNotAllowedApiResponse
    @Status(HttpStatus.NO_CONTENT)
    @Delete
    @Secured(VARIABLE_OWNER)
    fun deleteVariableDefinitionById(
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Delete", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        definitionId: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Delete",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
    ): MutableHttpResponse<Unit> {
        if (patches.latest(definitionId).variableStatus != VariableStatus.DRAFT) {
            throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published and cannot be updated with this method",
            )
        }

        patches.deleteAllForDefinitionId(definitionId)
        // Need to explicitly return a response as a workaround for https://github.com/micronaut-projects/micronaut-core/issues/9611
        return HttpResponse.noContent<Unit?>().contentType(null)
    }

    /**
     * Update a variable definition. Only the fields which need updating should be supplied.
     */
    @Tag(name = DRAFT)
    @ApiResponse(
        responseCode = "200",
        description = "Successfully updated",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Update",
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
                schema = Schema(implementation = CompleteResponse::class),
            ),
        ],
    )
    @BadRequestApiResponse
    @NotFoundApiResponse
    @MethodNotAllowedApiResponse
    @ConflictApiResponse
    @Patch
    @Secured(VARIABLE_OWNER)
    fun updateVariableDefinitionById(
        @Parameter(
            description = ID_FIELD_DESCRIPTION,
            examples = [
                ExampleObject(name = "Update", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        @PathVariable(VARIABLE_DEFINITION_ID_PATH_VARIABLE)
        definitionId: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Update",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        @RequestBody(
            content = [
                Content(
                    examples = [
                        ExampleObject(
                            name = "Update",
                            value = UPDATE_DRAFT_EXAMPLE,
                        ),
                        ExampleObject(
                            name = CONSTRAINT_VIOLATION_EXAMPLE_NAME,
                            value = UPDATE_DRAFT_CONSTRAINT_VIOLATION_EXAMPLE,
                        ),
                    ],
                    schema = Schema(implementation = UpdateDraft::class),
                ),
            ],
        )
        @Body
        @Valid
        updateDraft: UpdateDraft,
        authentication: Authentication,
    ): CompleteResponse {
        val variable = patches.latest(definitionId)

        when {
            (
                updateDraft.variableStatus == VariableStatus.PUBLISHED_INTERNAL ||
                    updateDraft.variableStatus == VariableStatus.PUBLISHED_EXTERNAL
            ) &&
                (
                    variable.shortName.contains(ILLEGAL_SHORNAME_KEYWORD) ||
                        updateDraft.shortName?.contains(ILLEGAL_SHORNAME_KEYWORD) == true
                ) -> {
                throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The short name ${variable.shortName} is illegal and must be changed before it is published",
                )
            }

            variable.variableStatus.isPublished() -> throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published and cannot be updated with this method",
            )

            (
                updateDraft.shortName != null &&
                    updateDraft.shortName != variable.shortName &&
                    vardef.doesShortNameExist(updateDraft.shortName)
            ) ->
                throw HttpStatusException(
                    HttpStatus.CONFLICT,
                    "The short name '${updateDraft.shortName}' is already in use by another variable definition.",
                )
            updateDraft.validUntil?.isBefore(variable.validFrom) == true -> throw HttpStatusException(
                HttpStatus.BAD_REQUEST,
                " Valid until can not be before valid from,",
            )
            updateDraft.validFrom?.isAfter(variable.validUntil) == true -> throw HttpStatusException(
                HttpStatus.BAD_REQUEST,
                " Valid from can not be after valid until,",
            )
            // make a check method
        }
        return vardef
            .update(variable, updateDraft, authentication.name)
            .toCompleteResponse()
    }
}

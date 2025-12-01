package no.ssb.metadata.vardef.controllers.internalapi

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
import no.ssb.metadata.vardef.models.RenderedOrCompleteUnion
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
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
@SecurityRequirement(name = LABID_TOKEN_SCHEME)
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
                    ExampleObject(name = "Rendered", value = RENDERED_VARIABLE_DEFINITION_EXAMPLE),
                ],
                oneOf = [Schema(implementation = CompleteResponse::class), Schema(implementation = RenderedVariableDefinition::class)],
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
                ExampleObject(name = "Rendered", value = ID_EXAMPLE),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ID_INVALID_EXAMPLE),
            ],
        )
        definitionId: String,
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(name = "Date not specified", value = ""),
                ExampleObject(name = "Specific date", value = DATE_EXAMPLE),
                ExampleObject(name = "Rendered", value = ""),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = ""),
            ],
        )
        @QueryValue("date_of_validity")
        dateOfValidity: LocalDate? = null,
        @Parameter(
            description = "Render the Variable Definition for presentation in a frontend",
            examples = [
                ExampleObject(name = "Date not specified", value = "false"),
                ExampleObject(name = "Specific date", value = "false"),
                ExampleObject(name = "Rendered", value = "true"),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = "false"),
            ],
        )
        @QueryValue("render")
        render: Boolean?,
    ): RenderedOrCompleteUnion =
        if (render == true) {
            vardef
                .getRenderedByDateAndStatus(
                    language = SupportedLanguages.NB,
                    definitionId = definitionId,
                    dateOfValidity = dateOfValidity,
                ).let { RenderedOrCompleteUnion.Rendered(it) }
        } else {
            vardef
                .getCompleteByDateAndStatus(
                    definitionId = definitionId,
                    dateOfValidity = dateOfValidity,
                )?.let { RenderedOrCompleteUnion.Complete(it) }
        } ?: throw HttpStatusException(
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
    ): MutableHttpResponse<Unit> {
        if (patches.latest(definitionId).variableStatus != VariableStatus.DRAFT) {
            throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published and cannot be updated with this method",
            )
        }

        patches.deleteAllForDefinitionId(definitionId)
        // Need to explicitly return a response as a workaround for https://github.com/micronaut-projects/micronaut-core/issues/9611
        return HttpResponse.noContent<Unit>().contentType(null)
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
        val existingVariable = patches.latest(definitionId)

        when {
            existingVariable.variableStatus.isPublished() -> {
                throw HttpStatusException(
                    HttpStatus.METHOD_NOT_ALLOWED,
                    "The variable is published and cannot be updated with this method",
                )
            }

            vardef.isIllegalShortNameForPublishing(existingVariable, updateDraft) -> {
                throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The short name ${existingVariable.shortName} is illegal and must be changed before it is published",
                )
            }

            vardef.isIllegalContactForPublishing(existingVariable, updateDraft) -> {
                throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The contact ${existingVariable.contact} is illegal and must be changed before it is published",
                )
            }

            (
                updateDraft.shortName != null &&
                    updateDraft.shortName != existingVariable.shortName &&
                    vardef.doesShortNameExist(updateDraft.shortName)
            ) -> {
                throw HttpStatusException(
                    HttpStatus.CONFLICT,
                    "The short name '${updateDraft.shortName}' is already in use by another variable definition.",
                )
            }

            !vardef.isCorrectDateOrderComparedToSaved(updateDraft, existingVariable) -> {
                throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date order",
                )
            }

            !vardef.allLanguagesPresentForExternalPublication(updateDraft, existingVariable) -> {
                throw HttpStatusException(
                    HttpStatus.CONFLICT,
                    "The variable must have translations for all languages for name, definition, comment before external publication.",
                )
            }
        }
        return vardef
            .update(existingVariable, updateDraft, authentication.name)
            .toCompleteResponse()
    }
}

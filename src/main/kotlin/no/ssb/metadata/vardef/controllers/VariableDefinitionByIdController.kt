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
import io.micronaut.security.rules.SecurityRule
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.UpdateDraft
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_OWNER
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId
import java.time.LocalDate

@Validated
@Controller("/variable-definitions/{definitionId}")
@Secured(VARIABLE_CONSUMER)
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionByIdController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Inject
    lateinit var patches: PatchesService

    /**
     * Get one variable definition.
     */
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [
                    ExampleObject(name = "No date specified", value = RENDERED_VARIABLE_DEFINITION_EXAMPLE),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get
    @Secured(VARIABLE_CONSUMER)
    @SecurityRequirement(name = "Bearer Authentication")
    fun getVariableDefinitionById(
        @Parameter(description = ID_FIELD_DESCRIPTION, example = ID_EXAMPLE)
        @VardefId
        definitionId: String,
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(name = "No date specified", value = ""),
                ExampleObject(name = "Specific date", value = DATE_EXAMPLE),
            ],
        )
        @QueryValue("date_of_validity")
        dateOfValidity: LocalDate? = null,
    ): CompleteResponse =
        varDefService
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
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @ApiResponse(responseCode = "405", description = "Attempt to delete a variable definition with status unlike DRAFT.")
    @Status(HttpStatus.NO_CONTENT)
    @Delete
    @Secured(VARIABLE_OWNER)
    @SecurityRequirement(name = "Bearer Authentication")
    fun deleteVariableDefinitionById(
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "delete", value = ID_EXAMPLE)])
        @VardefId
        definitionId: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "delete",
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
                "The variable is published or deprecated and cannot be updated with this method",
            )
        }

        val savedGroup = patches.latest(definitionId).owner
        if (!patches.isValidGroup(activeGroup, savedGroup)) {
            throw HttpStatusException(
                HttpStatus.FORBIDDEN,
                "Only members of the groups ${savedGroup.groups} are allowed to edit this variable",
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
    @ApiResponse(responseCode = "200", description = "Successfully updated")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @ApiResponse(responseCode = "405", description = "Attempt to patch a variable definition with status unlike DRAFT.")
    @ApiResponse(responseCode = "409", description = "Short name is already in use by another variable definition.")
    @Patch
    @Secured(VARIABLE_OWNER)
    @SecurityRequirement(name = "Bearer Authentication")
    fun updateVariableDefinitionById(
        @Schema(description = ID_FIELD_DESCRIPTION)
        @VardefId
        definitionId: String,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        @Body
        @Valid updateDraft: UpdateDraft,
    ): CompleteResponse {
        val variable = patches.latest(definitionId)
        if (variable.variableStatus != VariableStatus.DRAFT) {
            throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published or deprecated and cannot be updated with this method",
            )
        }

        val savedGroup = patches.latest(definitionId).owner
        if (!patches.isValidGroup(activeGroup, savedGroup)) {
            throw HttpStatusException(
                HttpStatus.FORBIDDEN,
                "Only members of the groups ${savedGroup.groups} are allowed to edit this variable",
            )
        }

        if (updateDraft.shortName != null && varDefService.doesShortNameExist(updateDraft.shortName)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "The short name '${updateDraft.shortName}' is already in use by another variable definition.",
            )
        }

        return varDefService
            .update(patches.latest(definitionId).copyAndUpdate(updateDraft))
            .toCompleteResponse()
    }
}

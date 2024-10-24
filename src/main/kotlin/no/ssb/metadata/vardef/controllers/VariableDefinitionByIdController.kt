package no.ssb.metadata.vardef.controllers

import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.UpdateDraft
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.security.VARIABLE_OWNER
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId
import java.time.LocalDate

@Validated
@Controller("/variable-definitions/{definitionId}")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionByIdController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Inject
    lateinit var patches: PatchesService

    /**
     * Get one variable definition.
     *
     * This is rendered in the given language, with the default being Norwegian Bokmål.
     */
    @Tag(name = PUBLIC)
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
    @Get()
    fun getVariableDefinitionById(
        @Parameter(description = ID_FIELD_DESCRIPTION, example = ID_EXAMPLE)
        @VardefId
        definitionId: String,
        @Parameter(
            description = ACCEPT_LANGUAGE_HEADER_PARAMETER_DESCRIPTION,
            examples = [ExampleObject(name = "No date specified", value = DEFAULT_LANGUAGE)],
        )
        @Header("Accept-Language", defaultValue = DEFAULT_LANGUAGE)
        language: SupportedLanguages,
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(name = "No date specified", value = ""),
                ExampleObject(name = "Specific date", value = DATE_EXAMPLE),
            ],
        )
        @QueryValue("date_of_validity")
        dateOfValidity: LocalDate? = null,
        request: HttpRequest<*>,
    ): MutableHttpResponse<out Any>? {
        val definition =
            varDefService
                .getByDateAndRender(
                    definitionId = definitionId,
                    language = language,
                    dateOfValidity = dateOfValidity,
                )
        if (definition == null) {
            throw HttpStatusException(
                HttpStatus.NOT_FOUND,
                "Variable is not valid at date $dateOfValidity",
            )
        }

        return HttpResponse
            .ok(definition)
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
            .contentType(MediaType.APPLICATION_JSON)
    }

    /**
     * Delete a variable definition.
     */
    @Tag(name = DRAFT)
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @ApiResponse(responseCode = "405", description = "Attempt to delete a variable definition with status unlike DRAFT.")
    @Status(HttpStatus.NO_CONTENT)
    @Delete()
    @Secured(VARIABLE_OWNER)
    fun deleteVariableDefinitionById(
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "delete", value = ID_EXAMPLE)])
        @VardefId
        definitionId: String,
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
    ): MutableHttpResponse<Unit> {
        if (patches.latest(definitionId).variableStatus != VariableStatus.DRAFT) {
            throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published or deprecated and cannot be updated with this method",
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
    fun updateVariableDefinitionById(
        @Schema(description = ID_FIELD_DESCRIPTION) @VardefId definitionId: String,
        @Body @Valid updateDraft: UpdateDraft,
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
    ): CompleteResponse {
        val variable = patches.latest(definitionId)
        if (variable.variableStatus != VariableStatus.DRAFT) {
            throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published or deprecated and cannot be updated with this method",
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

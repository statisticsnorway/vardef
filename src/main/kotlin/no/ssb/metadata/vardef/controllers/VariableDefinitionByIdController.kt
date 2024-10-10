package no.ssb.metadata.vardef.controllers

import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.http.annotation.Patch
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
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
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId
import java.time.LocalDate

@Validated
@Controller("/variable-definitions/{id}")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionByIdController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    /**
     * Get one variable definition.
     *
     * This is rendered in the given language, with the default being Norwegian Bokmål.
     */
    @Tag(name = PUBLIC)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(
        responseCode = "200",
        content = [Content(examples = [ExampleObject(name = "No date specified", value = RENDERED_VARIABLE_DEFINITION_EXAMPLE)])],
    )
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get()
    fun getVariableDefinitionById(
        @Parameter(description = ID_FIELD_DESCRIPTION, example = ID_EXAMPLE)
        @VardefId
        id: String,
        @Parameter(
            description = ACCEPT_LANGUAGE_HEADER_PARAMETER_DESCRIPTION,
            examples = [ExampleObject(name = "No date specified", value = DEFAULT_LANGUAGE)],
        )
        @Header("Accept-Language", defaultValue = DEFAULT_LANGUAGE)
        language: SupportedLanguages,
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            examples = [ExampleObject(name = "No date specified", value = ""), ExampleObject(name = "Specific date", value = DATE_EXAMPLE)],
        )
        @QueryValue("date_of_validity")
        dateOfValidity: LocalDate? = null,
    ): MutableHttpResponse<RenderedVariableDefinition?>? =
        HttpResponse
            .ok(
                varDefService.getOneByIdAndDateAndRenderForLanguage(
                    id = id,
                    language = language,
                    dateOfValidity = dateOfValidity,
                ),
            ).header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
            .contentType(MediaType.APPLICATION_JSON)

    /**
     * Delete a variable definition.
     */
    @Tag(name = DRAFT)
    @ApiResponse(responseCode = "204", description = "Successfully deleted")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Status(HttpStatus.NO_CONTENT)
    @Delete()
    fun deleteVariableDefinitionById(
        @Parameter(description = ID_FIELD_DESCRIPTION, examples = [ExampleObject(name = "delete", value = ID_EXAMPLE)])
        @VardefId
        id: String,
    ): MutableHttpResponse<Unit> {
        varDefService.deleteById(id = id)
        // Need to explicitly return a response as a workaround for https://github.com/micronaut-projects/micronaut-core/issues/9611
        return HttpResponse.noContent<Unit?>().contentType(null)
    }

    /**
     * Update a variable definition. Only the fields which need updating should be supplied.
     */
    @Tag(name = DRAFT)
    @ApiResponse(responseCode = "200", description = "Successfully updated")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @ApiResponse(responseCode = "405", description = "Attempt to patch a variable definition with status other than DRAFT.")
    @Patch
    fun updateVariableDefinitionById(
        @Schema(description = ID_FIELD_DESCRIPTION) @VardefId id: String,
        @Body @Valid updateDraft: UpdateDraft,
    ): CompleteResponse {
        val variable = varDefService.getLatestPatchById(id)
        if (variable.variableStatus != VariableStatus.DRAFT) {
            throw HttpStatusException(
                HttpStatus.METHOD_NOT_ALLOWED,
                "The variable is published or deprecated and cannot be updated with this method",
            )
        }
        return varDefService.update(varDefService.getLatestPatchById(id).copyAndUpdate(updateDraft)).toCompleteResponse()
    }
}

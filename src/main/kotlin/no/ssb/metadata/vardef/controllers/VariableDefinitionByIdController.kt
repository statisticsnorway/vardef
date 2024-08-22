package no.ssb.metadata.vardef.controllers

import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.constants.ID_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.UpdateVariableDefinition
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId

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
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Get()
    fun getVariableDefinitionById(
        @Schema(description = ID_FIELD_DESCRIPTION) @VardefId id: String,
        @Header("Accept-Language", defaultValue = "nb") language: SupportedLanguages,
    ): MutableHttpResponse<RenderedVariableDefinition?>? =
        HttpResponse
            .ok(varDefService.getOneByIdAndRenderForLanguage(id = id, language = language))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
            .contentType(MediaType.APPLICATION_JSON)

    /**
     * Delete a variable definition.
     */
    @ApiResponse(responseCode = "204", description = "Successfully deleted", content = [Content()])
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Status(HttpStatus.NO_CONTENT)
    @Delete()
    fun deleteVariableDefinitionById(
        @Schema(description = ID_FIELD_DESCRIPTION) @VardefId id: String,
    ): MutableHttpResponse<Unit> {
        varDefService.deleteById(id = id)
        // Need to explicitly return a response as a workaround for https://github.com/micronaut-projects/micronaut-core/issues/9611
        return HttpResponse.noContent<Unit?>().contentType(null)
    }

    /**
     * Update a variable definition. Only the fields which need updating should be supplied.
     */
    @ApiResponse(responseCode = "200", description = "Successfully updated")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Patch
    fun updateVariableDefinitionById(
        @Schema(description = ID_FIELD_DESCRIPTION) @VardefId id: String,
        @Body @Valid varDefUpdates: UpdateVariableDefinition,
    ): InputVariableDefinition =
        varDefService
            .update(varDefService.getLatestVersionById(id).copyAndUpdate(varDefUpdates))
            .toInputVariableDefinition()
}

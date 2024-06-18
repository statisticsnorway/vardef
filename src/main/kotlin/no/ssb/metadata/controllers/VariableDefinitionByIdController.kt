package no.ssb.metadata.controllers

import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.models.InputVariableDefinition
import no.ssb.metadata.models.RenderedVariableDefinition
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.UpdateVariableDefinition
import no.ssb.metadata.services.VariableDefinitionService
import no.ssb.metadata.validators.VardefId

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
        @VardefId id: String,
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
        @VardefId id: String,
    ): MutableHttpResponse<Unit> {
        varDefService.deleteById(id = id)
        // Need to explicitly return a response as a workaround for https://github.com/micronaut-projects/micronaut-core/issues/9611
        return HttpResponse.noContent<Unit?>().contentType(null)
    }

    /**
     * Update a variable definition.
     */
    @ApiResponse(responseCode = "200", description = "Successfully updated")
    @ApiResponse(responseCode = "404", description = "No such variable definition found")
    @Patch
    fun updateVariableDefinitionById(
        @VardefId id: String,
        @Body @Valid varDefUpdates: UpdateVariableDefinition,
    ): InputVariableDefinition {
        return varDefService.update(varDefService.getOneById(id).copyAndUpdate(varDefUpdates)).toInputVariableDefinition()
    }
}

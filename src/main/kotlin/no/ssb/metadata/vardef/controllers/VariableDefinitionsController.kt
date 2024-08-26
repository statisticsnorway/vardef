package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.services.VariableDefinitionService

@Validated
@Controller("/variable-definitions")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionsController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    /**
     * List all variable definitions.
     *
     * These are rendered in the given language, with the default being Norwegian Bokmål.
     */
    @Get()
    fun listVariableDefinitions(
        @Header("Accept-Language", defaultValue = "nb") language: SupportedLanguages,
    ): HttpResponse<List<RenderedVariableDefinition>> =
        HttpResponse
            .ok(varDefService.listAllAndRenderForLanguage(language))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())

    /**
     * Create a variable definition.
     *
     * New variable definitions are automatically assigned status DRAFT and must include all required fields.
     *
     * Attempts to specify id or variable_status in a request will receive 400 BAD REQUEST responses.
     */
    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    fun createVariableDefinition(
        @Body @Valid varDef: InputVariableDefinition,
    ): InputVariableDefinition {
        if (varDef.id != null) throw HttpStatusException(HttpStatus.BAD_REQUEST, "ID may not be specified on creation.")
        if (varDef.variableStatus != null) {
            throw HttpStatusException(
                HttpStatus.BAD_REQUEST,
                "Variable status may not be specified on creation.",
            )
        }

        return varDefService.save(varDef.toSavedVariableDefinition(null)).toInputVariableDefinition()
    }
}

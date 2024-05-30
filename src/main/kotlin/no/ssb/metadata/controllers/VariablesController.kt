package no.ssb.metadata.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.Valid
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.services.VariableDefinitionService

@Validated
@Controller("/variables")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariablesController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Get()
    fun listVariableDefinitions(
        @Header("Accept-Language", defaultValue = "nb") language: SupportedLanguages,
    ): HttpResponse<List<VariableDefinitionDTO>> {
        return HttpResponse
            .ok(varDefService.findByLanguage(language))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
    }

    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Successfully created.")
    @ApiResponse(responseCode = "400", description = "Bad request.")
    fun createVariableDefinition(
        @Body @Valid varDef: VariableDefinitionDAO,
    ): VariableDefinitionDAO = varDefService.saveVariable(varDef)
}

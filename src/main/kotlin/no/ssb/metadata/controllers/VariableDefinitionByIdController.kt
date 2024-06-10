package no.ssb.metadata.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Produces
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.inject.Inject
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.VARDEF_ID_PATTERN
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.services.VariableDefinitionService

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
        @Pattern(regexp = VARDEF_ID_PATTERN) id: String,
        @Header("Accept-Language", defaultValue = "nb") language: SupportedLanguages,
    ): MutableHttpResponse<VariableDefinitionDTO?>? =
        HttpResponse
            .ok(varDefService.getOneByIdAndRenderForLanguage(id = id, language = language))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
            .contentType(MediaType.APPLICATION_JSON)
}

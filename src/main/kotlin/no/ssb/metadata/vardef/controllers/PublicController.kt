package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.QueryValue
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.services.VariableDefinitionService
import java.time.LocalDate

@Tag(name = PUBLIC)
@Controller("/public")
@ExecuteOn(TaskExecutors.BLOCKING)
class PublicController(
    private val varDefService: VariableDefinitionService,
) {
    /**
     * List all variable definitions.
     *
     * These are rendered in the given language, with the default being Norwegian Bokmål.
     */
    @ApiResponse(
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "List of one variable definition",
                        value = LIST_OF_RENDERED_VARIABLE_DEFINITIONS_EXAMPLE,
                    ), ExampleObject(
                        name = "Empty list",
                        value = EMPTY_LIST_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @Get("/variable-definitions")
    fun listVariableDefinitions(
        @Parameter(description = ACCEPT_LANGUAGE_HEADER_PARAMETER_DESCRIPTION, example = DEFAULT_LANGUAGE)
        @Header("Accept-Language", defaultValue = DEFAULT_LANGUAGE)
        language: SupportedLanguages,
        @QueryValue("date_of_validity")
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            schema = Schema(format = DATE_FORMAT),
            examples = [ExampleObject(name = "Not specified", value = ""), ExampleObject(name = "Specific date", value = DATE_EXAMPLE)],
        )
        dateOfValidity: LocalDate? = null,
    ): HttpResponse<List<RenderedVariableDefinition>> =
        HttpResponse
            .ok(varDefService.listForDateAndRender(language = language, dateOfValidity = dateOfValidity))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
}

package no.ssb.metadata.vardef.controllers

import io.micronaut.http.*
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
import io.swagger.v3.oas.annotations.tags.Tag
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.services.ValidityPeriodsService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.validators.VardefId
import java.time.LocalDate

@Tag(name = PUBLIC)
@Validated
@Controller("/public")
@Secured(SecurityRule.IS_ANONYMOUS)
@ExecuteOn(TaskExecutors.BLOCKING)
class PublicController(
    private val varDefService: VariableDefinitionService,
    private val validityPeriods: ValidityPeriodsService,
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
    @Tag(name = VARIABLE_DEFINITIONS)
    @Get("/variable-definitions")
    fun listPublicVariableDefinitions(
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
            .ok(varDefService.listRenderedForDate(language = language, dateOfValidity = dateOfValidity))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())

    /**
     * Get one variable definition.
     *
     * This is rendered in the given language, with the default being Norwegian Bokmål.
     */
    @Tag(name = VARIABLE_DEFINITIONS)
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
    @Get("/variable-definitions/{definitionId}")
    fun getPublicVariableDefinitionById(
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
    ): MutableHttpResponse<RenderedVariableDefinition> {
        val definition =
            varDefService
                .getRenderedByDate(
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
     * List all validity periods.
     *
     * These are rendered in the given language, with the default being Norwegian Bokmål.
     */
    @Get("/variable-definitions/{variable-definition-id}/validity-periods")
    @Produces(MediaType.APPLICATION_JSON)
    @Tag(name = VALIDITY_PERIODS)
    @ApiResponse(
        responseCode = "200",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "???",
                        value = LIST_OF_RENDERED_VARIABLE_DEFINITIONS_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    fun listPublicValidityPeriods(
        @PathVariable("variable-definition-id")
        variableDefinitionId: String,
        @Header("Accept-Language", defaultValue = DEFAULT_LANGUAGE)
        language: SupportedLanguages,
    ): MutableHttpResponse<List<RenderedVariableDefinition>>? =
        HttpResponse
            .ok(validityPeriods.listRendered(language, variableDefinitionId))
            .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
            .contentType(MediaType.APPLICATION_JSON)
}

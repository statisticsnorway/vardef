package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
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
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.Draft
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.services.PatchesService
import no.ssb.metadata.vardef.services.VariableDefinitionService
import java.time.LocalDate

@Validated
@Controller("/variable-definitions")
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionsController {
    @Inject
    lateinit var varDefService: VariableDefinitionService

    @Inject
    lateinit var patches: PatchesService

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
    @Tag(name = PUBLIC)
    @Get()
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

    /**
     * Create a variable definition.
     *
     * New variable definitions are automatically assigned status DRAFT and must include all required fields.
     *
     * Attempts to specify id or variable_status in a request will receive 400 BAD REQUEST responses.
     */
    @Tag(name = DRAFT)
    @Post()
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created.",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Created variable definition",
                        value = DRAFT_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "400", description = "Bad request.")
    @ApiResponse(responseCode = "409", description = "Conflict.")
    fun createVariableDefinition(
        @Parameter(example = DRAFT_EXAMPLE) @Body @Valid varDef: Draft,
    ): CompleteResponse {
        if (varDef.id != null) throw HttpStatusException(HttpStatus.BAD_REQUEST, "ID may not be specified on creation.")
        if (varDef.variableStatus != null) {
            throw HttpStatusException(
                HttpStatus.BAD_REQUEST,
                "Variable status may not be specified on creation.",
            )
        }
        if (varDefService.checkIfShortNameExists(varDef.shortName)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Short name ${varDef.shortName} already exists.",
            )
        }

        return patches.save(varDef.toSavedVariableDefinition()).toCompleteResponse()
    }
}

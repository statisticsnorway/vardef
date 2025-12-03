package no.ssb.metadata.vardef.controllers.internalapi

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import no.ssb.metadata.vardef.annotations.BadRequestApiResponse
import no.ssb.metadata.vardef.annotations.ConflictApiResponse
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.CreateDraft
import no.ssb.metadata.vardef.models.RenderedOrCompleteUnion
import no.ssb.metadata.vardef.models.RenderedView
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_CREATOR
import no.ssb.metadata.vardef.services.VariableDefinitionService
import java.time.LocalDate

@Validated
@Controller("/variable-definitions")
@Secured(VARIABLE_CONSUMER)
@SecurityRequirement(name = LABID_TOKEN_SCHEME)
@ExecuteOn(TaskExecutors.BLOCKING)
class VariableDefinitionsController(
    private val vardef: VariableDefinitionService,
) {
    /**
     * List all variable definitions.
     */
    @Tag(name = VARIABLE_DEFINITIONS)
    @ApiResponse(
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Specific date",
                        value = LIST_OF_COMPLETE_VIEW_EXAMPLE,
                    ), ExampleObject(
                        name = "Specific short_name",
                        value = LIST_OF_COMPLETE_VIEW_EXAMPLE,
                    ), ExampleObject(
                        name = "Date not specified",
                        value = EMPTY_LIST_EXAMPLE,
                    ),
                    ExampleObject(name = "Rendered", value = LIST_OF_RENDERED_VIEWS_EXAMPLE),
                ],
                array = ArraySchema(items = Schema(name = "Rendered Or Complete", oneOf = [CompleteView::class, RenderedView::class])),
            ),
        ],
    )
    @Get
    fun listVariableDefinitions(
        @Parameter(description = ACCEPT_LANGUAGE_HEADER_PARAMETER_DESCRIPTION, example = DEFAULT_LANGUAGE)
        @Header(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = DEFAULT_LANGUAGE)
        language: SupportedLanguages,
        @QueryValue("date_of_validity")
        @Parameter(
            description = DATE_OF_VALIDITY_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Specific date",
                    value = DATE_EXAMPLE,
                ),
            ],
        )
        dateOfValidity: LocalDate? = null,
        @QueryValue("short_name")
        @Parameter(
            description = SHORT_NAME_QUERY_PARAM_DESCRIPTION,
            examples = [
                ExampleObject(name = "Specific short_name", value = SHORT_NAME_EXAMPLE),
            ],
        )
        shortName: String? = null,
        @Parameter(
            description = "Render the Variable Definition for presentation in a frontend",
            examples = [
                ExampleObject(name = "Date not specified", value = "false"),
                ExampleObject(name = "Specific date", value = "false"),
                ExampleObject(name = "Rendered", value = "true"),
                ExampleObject(name = NOT_FOUND_EXAMPLE_NAME, value = "false"),
            ],
        )
        @QueryValue("render")
        render: Boolean?,
    ): MutableHttpResponse<List<RenderedOrCompleteUnion>> =
        if (render == true) {
            vardef
                .listRenderedForDate(language = language, dateOfValidity = dateOfValidity, shortName = shortName)
                .map { RenderedOrCompleteUnion.Rendered(it) }
        } else {
            vardef
                .listCompleteForDate(dateOfValidity = dateOfValidity, shortName = shortName)
                .map { RenderedOrCompleteUnion.Complete(it) }
        }.let {
            HttpResponse
                .ok(it)
                .header(HttpHeaders.CONTENT_LANGUAGE, language.toString())
                .contentType(MediaType.APPLICATION_JSON)
        }

    /**
     * Create a variable definition.
     *
     * New variable definitions are automatically assigned status DRAFT and must include all required fields.
     *
     * Attempts to specify id or variable_status in a request will receive 400 BAD REQUEST responses.
     */
    @Tag(name = DRAFT)
    @Post
    @Status(HttpStatus.CREATED)
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created.",
        content = [
            Content(
                examples = [
                    ExampleObject(
                        name = "Create Draft",
                        value = COMPLETE_VIEW_EXAMPLE,
                    ),
                ],
                schema = Schema(implementation = CompleteView::class),
            ),
        ],
    )
    @BadRequestApiResponse
    @ConflictApiResponse
    @Secured(VARIABLE_CREATOR)
    fun createVariableDefinition(
        @RequestBody(
            content = [
                Content(
                    examples = [
                        ExampleObject(
                            name = "Create Draft",
                            value = CREATE_DRAFT_EXAMPLE,
                        ),
                    ],
                    schema = Schema(implementation = CreateDraft::class),
                ),
            ],
        )
        @Body
        @Valid createDraft: CreateDraft,
        authentication: Authentication,
    ): CompleteView {
        if (vardef.doesShortNameExist(createDraft.shortName)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Short name ${createDraft.shortName} already exists.",
            )
        }

        val resolvedActiveGroup =
            (authentication.attributes[ACTIVE_GROUP] as? String)
                ?: throw HttpStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No active_group provided",
                )

        return vardef
            .create(createDraft.toSavedVariableDefinition(resolvedActiveGroup, authentication.name))
            .toCompleteView()
    }
}

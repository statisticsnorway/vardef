package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
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
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.Draft
import no.ssb.metadata.vardef.security.VARIABLE_CONSUMER
import no.ssb.metadata.vardef.security.VARIABLE_CREATOR
import no.ssb.metadata.vardef.services.VariableDefinitionService
import java.time.LocalDate

@Validated
@Controller("/variable-definitions")
@Secured(VARIABLE_CONSUMER)
@SecurityRequirement(name = KEYCLOAK_TOKEN_SCHEME)
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
                        value = LIST_OF_COMPLETE_RESPONSE_EXAMPLE,
                    ), ExampleObject(
                        name = "Date not specified",
                        value = EMPTY_LIST_EXAMPLE,
                    ),
                ],
                array = ArraySchema(schema = Schema(implementation = CompleteResponse::class)),
            ),
        ],
    )
    @Get
    fun listVariableDefinitions(
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
                ExampleObject(name = "landbak", value = SHORT_NAME_EXAMPLE),
            ],
        )
        shortName: String? = null,
    ): List<CompleteResponse> = vardef.listCompleteForDate(dateOfValidity = dateOfValidity, shortName = shortName)

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
                        name = "Create draft",
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
                schema = Schema(implementation = CompleteResponse::class),
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
                            name = "Create draft",
                            value = DRAFT_EXAMPLE,
                        ),
                    ],
                    schema = Schema(implementation = Draft::class),
                ),
            ],
        )
        @Body
        @Valid draft: Draft,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "Create draft",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
        authentication: Authentication,
    ): CompleteResponse {
        if (vardef.doesShortNameExist(draft.shortName)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Short name ${draft.shortName} already exists.",
            )
        }
        return vardef.create(draft.toSavedVariableDefinition(activeGroup, authentication.name)).toCompleteResponse()
    }
}

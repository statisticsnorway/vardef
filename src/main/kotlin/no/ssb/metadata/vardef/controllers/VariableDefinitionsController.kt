package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.*
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.security.annotation.Secured
import io.micronaut.validation.Validated
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
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
                        name = "List of one variable definition",
                        value = LIST_OF_COMPLETE_RESPONSE_EXAMPLE,
                    ), ExampleObject(
                        name = "Empty list",
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
            schema = Schema(format = DATE_FORMAT),
            examples = [ExampleObject(name = "Not specified", value = ""), ExampleObject(name = "Specific date", value = DATE_EXAMPLE)],
        )
        dateOfValidity: LocalDate? = null,
    ): List<CompleteResponse> = vardef.listCompleteForDate(dateOfValidity = dateOfValidity)

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
                        name = "create_draft",
                        value = COMPLETE_RESPONSE_EXAMPLE,
                    ),
                ],
                schema = Schema(implementation = CompleteResponse::class),
            ),
        ],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Malformed data, missing data or attempt to specify disallowed fields.",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = HttpClientException::class),
                examples = [
                    ExampleObject(
                        name = "bad_request",
                        value = BAD_REQUEST_ERROR_RESPONSE_EXAMPLE,
                    ),
                ],
            ),
        ],
    )
    @ApiResponse(responseCode = "409", description = "Short name is already in use by another variable definition.")
    @Secured(VARIABLE_CREATOR)
    fun createVariableDefinition(
        @Parameter(
            examples = [
                ExampleObject(
                    name = "create_draft",
                    value = DRAFT_EXAMPLE,
                ),
                ExampleObject(
                    name = "bad_request",
                    value = DRAFT_INVALID_UNIT_TYPE_EXAMPLE,
                ),
            ],
            schema = Schema(implementation = Draft::class),
        )
        @Body
        @Valid draft: Draft,
        @Parameter(
            name = ACTIVE_GROUP,
            description = ACTIVE_GROUP_QUERY_PARAMETER_DESCRIPTION,
            examples = [
                ExampleObject(
                    name = "create_draft",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
                ExampleObject(
                    name = "bad_request",
                    value = ACTIVE_GROUP_EXAMPLE,
                ),
            ],
        )
        @QueryValue(ACTIVE_GROUP)
        activeGroup: String,
    ): CompleteResponse {
        if (vardef.doesShortNameExist(draft.shortName)) {
            throw HttpStatusException(
                HttpStatus.CONFLICT,
                "Short name ${draft.shortName} already exists.",
            )
        }
        return vardef.create(draft.toSavedVariableDefinition(activeGroup)).toCompleteResponse()
    }
}

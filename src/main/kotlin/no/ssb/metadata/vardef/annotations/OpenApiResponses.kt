package no.ssb.metadata.vardef.annotations

import io.micronaut.problem.ProblemErrorResponseProcessor.APPLICATION_PROBLEM_JSON
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.ssb.metadata.vardef.constants.*

const val PROBLEM_JSON_SCHEMA_REF = "https://opensource.zalando.com/restful-api-guidelines/models/problem-1.0.1.yaml#/Problem"

@ApiResponse(
    responseCode = "400",
    description = "Bad request.",
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema =
                Schema(ref = PROBLEM_JSON_SCHEMA_REF),
            examples = [
                ExampleObject(
                    name = "Bad request",
                    value = PROBLEM_JSON_BAD_REQUEST_EXAMPLE,
                ),
                ExampleObject(
                    name = "Constraint violation",
                    value = PROBLEM_JSON_CONSTRAINT_VIOLATION_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class BadRequestApiResponse

@ApiResponse(
    responseCode = "409",
    description = "Short name is already in use by another variable definition.",
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema =
                Schema(ref = PROBLEM_JSON_SCHEMA_REF),
            examples = [
                ExampleObject(
                    name = "Conflict",
                    value = PROBLEM_JSON_CONFLICT_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class ConflictApiResponse

@ApiResponse(
    responseCode = "405",
    description = "Not allowed for variable definitions with this status.",
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema =
                Schema(ref = PROBLEM_JSON_SCHEMA_REF),
            examples = [
                ExampleObject(
                    name = "Method not allowed",
                    value = PROBLEM_JSON_METHOD_NOT_ALLOWED_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class MethodNotAllowedApiResponse

@ApiResponse(
    responseCode = "404",
    description = "Not found",
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema =
                Schema(ref = PROBLEM_JSON_SCHEMA_REF),
            examples = [
                ExampleObject(
                    name = "Not found",
                    value = PROBLEM_JSON_NOT_FOUND_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class NotFoundApiResponse

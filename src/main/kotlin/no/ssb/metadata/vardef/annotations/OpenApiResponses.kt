package no.ssb.metadata.vardef.annotations

import io.micronaut.problem.ProblemErrorResponseProcessor.APPLICATION_PROBLEM_JSON
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.ssb.metadata.vardef.constants.*
import org.zalando.problem.Problem

@ApiResponse(
    responseCode = "400",
    description = "Bad request.",
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema = Schema(implementation = Problem::class),
            examples = [
                ExampleObject(
                    name = BAD_REQUEST_EXAMPLE_NAME,
                    value = PROBLEM_JSON_BAD_REQUEST_EXAMPLE,
                ),
                ExampleObject(
                    name = CONSTRAINT_VIOLATION_EXAMPLE_NAME,
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
            schema = Schema(implementation = Problem::class),
            examples = [
                ExampleObject(
                    name = CONFLICT_EXAMPLE_NAME,
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
            schema = Schema(implementation = Problem::class),
            examples = [
                ExampleObject(
                    name = METHOD_NOT_ALLOWED_EXAMPLE_NAME,
                    value = PROBLEM_JSON_METHOD_NOT_ALLOWED_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class MethodNotAllowedApiResponse

@ApiResponse(
    responseCode = "404",
    description = NOT_FOUND_EXAMPLE_NAME,
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema = Schema(implementation = Problem::class),
            examples = [
                ExampleObject(
                    name = NOT_FOUND_EXAMPLE_NAME,
                    value = PROBLEM_JSON_NOT_FOUND_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class NotFoundApiResponse

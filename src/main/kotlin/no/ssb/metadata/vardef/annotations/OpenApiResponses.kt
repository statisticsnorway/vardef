package no.ssb.metadata.vardef.annotations

import io.micronaut.problem.ProblemErrorResponseProcessor.APPLICATION_PROBLEM_JSON
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.ssb.metadata.vardef.constants.NOT_FOUND_EXAMPLE

const val PROBLEM_JSON_SCHEMA = "https://opensource.zalando.com/restful-api-guidelines/models/problem-1.0.1.yaml#/Problem"

@ApiResponse(
    responseCode = "404",
    description = "Not found",
    content = [
        Content(
            mediaType = APPLICATION_PROBLEM_JSON,
            schema =
                Schema(ref = PROBLEM_JSON_SCHEMA),
            examples = [
                ExampleObject(
                    name = "Not found",
                    value = NOT_FOUND_EXAMPLE,
                ),
            ],
        ),
    ],
)
annotation class NotFoundApiResponse

package no.ssb.metadata.vardef.handlers

import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton

/**
 * Illegal argument exception handler.
 *
 * This is primarily intended to prevent invalid values for VariableStatus from returning 500 HTTP responses. It
 * returns a 400 HTTP response instead, to make it clear to the user that there was a problem with the provided data.
 *
 * If this is found to have unintended consequences, it could be made more specific by examining the exception message.
 *
 * @property errorResponseProcessor
 * @constructor Create empty Illegal argument exception handler
 */
@Produces
@Singleton
@Requirements(
    Requires(classes = [IllegalArgumentException::class, ExceptionHandler::class]),
)
class IllegalArgumentExceptionHandler(
    private val errorResponseProcessor: ErrorResponseProcessor<*>,
) : ExceptionHandler<IllegalArgumentException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: IllegalArgumentException,
    ): HttpResponse<*> =
        errorResponseProcessor.processResponse(
            ErrorContext
                .builder(request)
                .cause(exception)
                .errorMessage(exception.message)
                .build(),
            HttpResponse.badRequest<Any>(),
        )
}

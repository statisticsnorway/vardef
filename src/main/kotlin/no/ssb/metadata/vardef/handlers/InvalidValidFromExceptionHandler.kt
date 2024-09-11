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
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException

@Produces
@Singleton
@Requirements(
    Requires(classes = [InvalidValidFromException::class, ExceptionHandler::class]),
)
class InvalidValidFromExceptionHandler(
    private val errorResponseProcessor: ErrorResponseProcessor<Any>,
) : ExceptionHandler<InvalidValidFromException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: InvalidValidFromException,
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

package no.ssb.metadata.vardef.handlers

import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.data.exceptions.EmptyResultException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton

@Produces
@Singleton
@Requirements(
    Requires(classes = [EmptyResultException::class, ExceptionHandler::class]),
)
class EmptyResultExceptionHandler(
    private val errorResponseProcessor: ErrorResponseProcessor<Any>,
) : ExceptionHandler<EmptyResultException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: EmptyResultException,
    ): HttpResponse<*> =
        errorResponseProcessor.processResponse(
            ErrorContext
                .builder(request)
                .cause(exception)
                .errorMessage("No such variable definition found")
                .build(),
            HttpResponse.notFound<Any>(),
        )
}

package no.ssb.metadata.handlers

import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton
import java.time.format.DateTimeParseException

@Produces
@Singleton
@Requirements(
    Requires(classes = [DateTimeParseException::class, ExceptionHandler::class]),
)
class DateTimeParseExceptionHandler(private val errorResponseProcessor: ErrorResponseProcessor<Any>) :
    ExceptionHandler<DateTimeParseException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: DateTimeParseException,
    ): HttpResponse<*> {
        return errorResponseProcessor.processResponse(
            ErrorContext.builder(request)
                .cause(exception)
                .errorMessage("Invalid date format, a valid date follows this format: YYYY-MM-DD")
                .build(),
            HttpResponse.badRequest<Any>(),
        )
    }
}

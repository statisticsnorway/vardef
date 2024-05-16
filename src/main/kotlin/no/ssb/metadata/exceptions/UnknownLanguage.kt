package no.ssb.metadata.exceptions

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton

class UnknownLanguageException(override val message: String) : IllegalArgumentException()

@Produces
@Singleton
@Requires(classes = [UnknownLanguageException::class, ExceptionHandler::class])
class UnknownLanguageExceptionHandler(private val errorResponseProcessor: ErrorResponseProcessor<Any>) :
    ExceptionHandler<UnknownLanguageException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>?,
        exception: UnknownLanguageException?,
    ): HttpResponse<*> {
        return errorResponseProcessor.processResponse(
            ErrorContext.builder(request)
                .cause(exception)
                .errorMessage(exception?.message)
                .build(),
            HttpResponse.badRequest<Any>(),
        ) // (1)
    }
}

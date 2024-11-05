package no.ssb.metadata.vardef.handlers

import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.http.server.exceptions.response.ErrorContext
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor
import jakarta.inject.Singleton

/**
 * Handler for overriding ConversionErrorException to enable custom message
 * when trying to post fields not available at patches endpoint:
 * valid_from and short_name
 * Annotated with Primary to set order and choose custom before
 * default ConversionErrorExceptionHandler
 *
 * @property errorResponseProcessor
 */
@Produces
@Primary
@Singleton
@Requirements(
    Requires(classes = [ConversionErrorException::class, ExceptionHandler::class]),
)
class PropertyConversionErrorHandler(
    private val errorResponseProcessor: ErrorResponseProcessor<*>,
) : ExceptionHandler<ConversionErrorException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: ConversionErrorException,
    ): HttpResponse<*> =
        errorResponseProcessor.processResponse(
            ErrorContext
                .builder(request)
                .cause(exception)
                .errorMessage(customiseMessage(exception))
                .build(),
            HttpResponse.badRequest<Any>(),
        )
}

private fun customiseMessage(exception: ConversionErrorException): String? {
    val message = exception.cause?.message ?: ""
    return when {
        "Unknown property [valid_from]" in message -> "valid_from may not be specified here"
        "Unknown property [valid_until]" in message -> "valid_until may not be specified here"
        "Unknown property [short_name]" in message -> "short_name may not be specified here"
        "Unable to deserialize type [class no.ssb.metadata.vardef.models.Owner]" in message ->
            "owner team and groups can not be null"
        else -> exception.message
    }
}

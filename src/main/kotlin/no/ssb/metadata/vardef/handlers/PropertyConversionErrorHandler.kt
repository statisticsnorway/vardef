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
import io.micronaut.serde.exceptions.SerdeException
import jakarta.inject.Singleton

/**
 * Handler for overriding ConversionErrorException to enable custom message
 * when trying to post fields not available at patches endpoint:
 * valid_from and short_name
 * Annotated with Primary to set order and choose custom before
 * default ConversionErrorExceptionHandler
 */
@Produces
@Primary
@Singleton
@Requirements(
    Requires(classes = [ConversionErrorException::class, ExceptionHandler::class]),
)
class PropertyConversionErrorHandler(
    private val errorResponseProcessor: ErrorResponseProcessor<*>,
    ): ExceptionHandler<ConversionErrorException, HttpResponse<*>>{
        override fun handle(
            request: HttpRequest<*>,
            exception: ConversionErrorException
        ): HttpResponse<*> {
            val cause = exception.cause
            var message = exception.message.toString()
            if (cause != null) {
                if (cause is SerdeException) {
                    message = if(cause.message?.contains("Unknown property [valid_from]") == true){
                        "Valid from is not allowed at patches endpoint"
                    } else{
                        cause.message.toString()
                    }
                }
            }
            return  errorResponseProcessor.processResponse(
                ErrorContext
                    .builder(request)
                    .cause(exception)
                    .errorMessage(message)
                    .build(),
                HttpResponse.badRequest<Any>(),
            )
        }
    }

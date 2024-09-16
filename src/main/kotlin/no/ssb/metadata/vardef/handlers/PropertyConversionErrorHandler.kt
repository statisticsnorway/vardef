package no.ssb.metadata.vardef.handlers

import io.micronaut.context.annotation.Primary
import io.micronaut.core.convert.exceptions.ConversionErrorException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.exceptions.SerdeException
import jakarta.inject.Singleton

@Produces
@Primary
@Singleton
class PropertyConversionErrorHandler() : ExceptionHandler<ConversionErrorException, HttpResponse<ErrorResponse>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: ConversionErrorException,
    ): HttpResponse<ErrorResponse> {
        val cause = exception.cause

        if (cause != null) {
            return if (cause is SerdeException) {
                if (cause.message?.contains("Unknown property [valid_from]") == true) {
                    HttpResponse.badRequest(
                        ErrorResponse("Custom Error: Valid from is not allowed", HttpStatus.BAD_REQUEST.code),
                    )
                } else {
                    HttpResponse.badRequest(
                        ErrorResponse("Custom Error: Invalid data format", HttpStatus.BAD_REQUEST.code),
                    )
                }
            } else {
                return HttpResponse.badRequest(
                    ErrorResponse(cause.toString(), HttpStatus.BAD_REQUEST.code),
                )
            }
        } else {
            return HttpResponse.badRequest(
                ErrorResponse("Outside all", HttpStatus.BAD_REQUEST.code),
            )
        }
    }
}

// Data class to represent error response
@Serdeable
data class ErrorResponse(val message: String, val code: Int)

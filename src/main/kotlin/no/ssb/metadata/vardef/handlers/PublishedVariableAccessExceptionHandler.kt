package no.ssb.metadata.vardef.handlers

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.exceptions.PublishedVariableAccessException

@Produces
@Singleton
class PublishedVariableAccessExceptionHandler : ExceptionHandler<PublishedVariableAccessException, HttpResponse<*>> {
    override fun handle(
        request: HttpRequest<*>,
        exception: PublishedVariableAccessException,
    ): HttpResponse<*> {
        return HttpResponse.status<String>(HttpStatus.METHOD_NOT_ALLOWED)
            .body(exception.message)
    }
}

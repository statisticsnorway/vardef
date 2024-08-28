package no.ssb.metadata.vardef.handlers

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException

@Produces
@Singleton
class DefinitionTextUnchangedExceptionHandler: ExceptionHandler<DefinitionTextUnchangedException, HttpResponse<*>>  {
    override fun handle(request: HttpRequest<*>, exception: DefinitionTextUnchangedException): HttpResponse<*> {
        return HttpResponse.status<String>(HttpStatus.BAD_REQUEST)
            .body(exception.message)
    }

}


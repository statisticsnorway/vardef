package no.ssb.metadata.vardef.integrations.vardok.services

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse

@Client(id = "vardok")
@Produces(MediaType.APPLICATION_XML)
@Header(name = ACCEPT, value = "application/xml")
interface VardokClient {
    @Produces(MediaType.APPLICATION_XML)
    @Get("{id}")
    @SingleResult
    fun fetchVardokById(id: String): VardokResponse

    @Produces(MediaType.APPLICATION_XML)
    @Get("{id}/{language}")
    @SingleResult
    fun fetchVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse
}

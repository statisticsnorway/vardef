package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client
@Produces(MediaType.APPLICATION_XML)
@Header(name = ACCEPT, value = "application/xml")
interface VarDokClient {
    @Produces(MediaType.APPLICATION_XML)
    @Get("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/{id}")
    @SingleResult
    fun fetchVarDokById(id: String): FIMD

    @Produces(MediaType.APPLICATION_XML)
    @Get("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/{id}/{language}")
    @SingleResult
    fun fetchVarDokByIdAndLanguage(
        id: String,
        language: String,
    ): FIMD
}

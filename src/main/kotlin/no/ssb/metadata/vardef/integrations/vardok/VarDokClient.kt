package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
interface VarDokClient {

    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Get("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/1422/nb")
    @SingleResult
    fun fetchVarDok(): FIMD
}
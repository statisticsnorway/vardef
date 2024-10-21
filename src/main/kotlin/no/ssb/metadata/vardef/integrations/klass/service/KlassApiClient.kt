package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpHeaders.ACCEPT
import io.micronaut.http.HttpHeaders.USER_AGENT
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Codes
import no.ssb.metadata.vardef.models.SupportedLanguages

/**
 * A declarative client for the Klass API
 */
@Client(id = "klass")
@Headers(
    Header(name = USER_AGENT, value = "VarDef API"),
    Header(name = ACCEPT, value = "application/json"),
)
interface KlassApiClient {
    @Get("classifications/{classificationId}")
    @SingleResult
    fun fetchClassification(
        @PathVariable classificationId: Int,
    ): HttpResponse<Classification?>

    @Get("classifications/{classificationId}/codesAt?date={codesAt}&language={language}")
    @SingleResult
    fun listCodes(
        @PathVariable classificationId: Int,
        @QueryValue codesAt: String,
        @QueryValue language: SupportedLanguages,
    ): HttpResponse<Codes>
}

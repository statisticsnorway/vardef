package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

class VardefTokenValidatorTest {

    @Inject
    lateinit var vardefTokenValidator: VardefTokenValidator<HttpRequest<*>>

    @Test
    fun `verify token comes from dapla lab`(){

    }
}
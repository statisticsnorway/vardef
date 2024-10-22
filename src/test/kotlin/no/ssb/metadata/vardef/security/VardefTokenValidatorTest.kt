package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.JwtTokenHelper
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test


@MicronautTest
class VardefTokenValidatorTest {

    @Inject
    lateinit var vardefTokenValidator: VardefTokenValidator<HttpRequest<*>>

    @Test
    fun `verify token comes from dapla lab`(){
        val result = vardefTokenValidator.validate(JwtTokenHelper.jwtTokenSigned().parsedString, HttpRequest.POST("/vardef",""))
        assertThat(result).isNotNull
    }
}
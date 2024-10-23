package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.JwtTokenHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

@MicronautTest
class VardefTokenValidatorTest {
    @Inject
    lateinit var vardefTokenValidator: VardefTokenValidator<HttpRequest<*>>

    @Test
    fun `role assigned if request comes from dapla lab`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(JwtTokenHelper.jwtTokenSigned().parsedString, HttpRequest.POST("/vardef", "")),
                ).block()
        assertThat(auth?.roles).containsExactly(Roles.VARIABLE_OWNER.name)
    }

    @Test
    fun `role assigned if request doesn't come from dapla lab`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned(listOf("blah")).parsedString,
                        HttpRequest.POST("/vardef", ""),
                    ),
                ).block()
        assertThat(auth?.roles).doesNotContain(Roles.VARIABLE_OWNER.name)
        assertThat(auth?.roles).contains(Roles.VARIABLE_CONSUMER.name)
    }
}

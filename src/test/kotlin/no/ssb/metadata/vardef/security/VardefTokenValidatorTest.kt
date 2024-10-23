package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.JwtTokenHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono

@MicronautTest
class VardefTokenValidatorTest {
    @Inject
    lateinit var vardefTokenValidator: VardefTokenValidator<MutableHttpRequest<*>>

    @Test
    fun `request comes from dapla lab and valid active group supplied`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned().parsedString,
                        HttpRequest.POST("/vardef?active_group=play-enhjoern-a-developers", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(Roles.VARIABLE_OWNER.name)
    }

    @Test
    fun `request doesn't come from dapla lab`() {
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

    @Test
    fun `active group not present in token`() {
        assertThrows<RuntimeException> {
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned().parsedString,
                        HttpRequest.POST("/vardef?active_group=unknown-developers", ""),
                    ),
                ).block()
        }
    }
}

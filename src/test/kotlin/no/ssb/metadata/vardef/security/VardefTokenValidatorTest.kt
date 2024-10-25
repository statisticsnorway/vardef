package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.ACTIVE_TEAM
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
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
                        HttpRequest.POST("/variable-definitions?$ACTIVE_GROUP=play-enhjoern-a-developers&$ACTIVE_TEAM=play-enhjoern-a", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(VARIABLE_OWNER)
    }

    @Test
    fun `request doesn't come from dapla lab`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned(listOf("blah")).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.roles).doesNotContain(VARIABLE_OWNER)
        assertThat(auth?.roles).contains(VARIABLE_CONSUMER)
    }

    @Test
    fun `active group not present in token`() {
        assertThrows<InvalidActiveGroupException> {
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned().parsedString,
                        HttpRequest.POST("/variable-definitions?$ACTIVE_GROUP=unknown-developers", ""),
                    ),
                ).block()
        }
    }

    @Test
    fun `token is null`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        null,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()

        assertThat(auth).isNull()
    }

    @Test
    fun `active team is not in token`() {
        val result =
            vardefTokenValidator.isValidTeam(
                HttpRequest.POST(
                    "/variable-definitions?$ACTIVE_TEAM=team-a",
                    "",
                ),
                JwtTokenHelper.jwtTokenSigned(),
            )
        assertThat(result).isFalse
    }

    @Test
    fun `active team is in token`() {
        val result =
            vardefTokenValidator.isValidTeam(
                HttpRequest.POST(
                    "/variable-definitions?$ACTIVE_TEAM=play-foeniks-a",
                    "",
                ),
                JwtTokenHelper.jwtTokenSigned(),
            )
        assertThat(result).isTrue
    }
}

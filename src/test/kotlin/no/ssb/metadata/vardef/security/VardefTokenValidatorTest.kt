package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import no.ssb.metadata.vardef.utils.JwtTokenHelper
import no.ssb.metadata.vardef.utils.TEST_DEVELOPERS_GROUP
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
                        HttpRequest.POST("/variable-definitions?$ACTIVE_GROUP=$TEST_DEVELOPERS_GROUP", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER, VARIABLE_OWNER, VARIABLE_CREATOR)
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
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
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
    fun `token does not contain dapla groups`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned(daplaGroups = null).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()

        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
    }

    @Test
    fun `no dapla structure in token`() {
        val auth =
            Mono
                .from(
                    vardefTokenValidator.validateToken(
                        JwtTokenHelper.jwtTokenSigned(includeDaplaStructure = false).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()

        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
    }
}

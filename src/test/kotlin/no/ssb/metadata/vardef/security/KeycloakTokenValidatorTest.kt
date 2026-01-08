package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.KeycloakTokenHelper
import no.ssb.metadata.vardef.utils.TEST_USER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

@MicronautTest
class KeycloakTokenValidatorTest {
    @Inject
    lateinit var keycloakTokenValidator: KeycloakTokenValidator<MutableHttpRequest<*>>

    @Test
    fun `request with malformed token`() {
        val auth =
            Mono
                .from(
                    keycloakTokenValidator.validateToken(
                        "not a token",
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth).isNull()
    }

    @Test
    fun `valid token`() {
        val auth =
            Mono
                .from(
                    keycloakTokenValidator.validateToken(
                        KeycloakTokenHelper.tokenSigned().serialize(),
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
        assertThat(auth?.name).isEqualTo(TEST_USER)
    }

    @Test
    fun `request token doesn't contain allowed audience`() {
        val auth =
            Mono
                .from(
                    keycloakTokenValidator.validateToken(
                        KeycloakTokenHelper.tokenSigned(audienceClaim = listOf("blah")).serialize(),
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth).isNull()
    }

    @Test
    fun `token is null`() {
        val auth =
            Mono
                .from(
                    keycloakTokenValidator.validateToken(
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
                    keycloakTokenValidator.validateToken(
                        KeycloakTokenHelper.tokenSigned(daplaGroups = null).serialize(),
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()

        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
    }

    @Test
    fun `token does not have sub claim`() {
        val auth =
            Mono
                .from(
                    keycloakTokenValidator.validateToken(
                        KeycloakTokenHelper.tokenSigned(includeUsername = false).serialize(),
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth).isNull()
    }
}

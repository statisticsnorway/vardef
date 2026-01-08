package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.LabIdTokenHelper
import no.ssb.metadata.vardef.utils.TEST_USER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

@MicronautTest
class LabIdTokenValidatorTest {
    @Inject
    lateinit var labidTokenValidator: LabIdTokenValidator<MutableHttpRequest<*>>

    @Test
    fun `request with malformed token`() {
        val auth =
            Mono
                .from(
                    labidTokenValidator.validateToken(
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
                    labidTokenValidator.validateToken(
                        LabIdTokenHelper.tokenSigned().parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER, VARIABLE_OWNER, VARIABLE_CREATOR)
        assertThat(auth?.name).isEqualTo(TEST_USER)
    }

    @Test
    fun `request token doesn't contain allowed audience`() {
        val auth =
            Mono
                .from(
                    labidTokenValidator.validateToken(
                        LabIdTokenHelper.tokenSigned(audienceClaim = listOf("blah")).parsedString,
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
                    labidTokenValidator.validateToken(
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
                    labidTokenValidator.validateToken(
                        LabIdTokenHelper.tokenSigned(daplaGroups = null).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()

        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
    }

    @Test
    fun `authentication object does not contain username`() {
        val auth =
            Mono
                .from(
                    labidTokenValidator.validateToken(
                        LabIdTokenHelper.tokenSigned(includeUsername = false).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth).isNull()
    }
}

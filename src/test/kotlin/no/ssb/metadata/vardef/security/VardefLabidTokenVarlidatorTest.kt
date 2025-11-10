package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpRequest
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.exceptions.InvalidActiveGroupException
import no.ssb.metadata.vardef.utils.LabIdTokenHelper
import no.ssb.metadata.vardef.utils.TEST_USER
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import reactor.core.publisher.Mono


@MicronautTest
class VardefLabidTokenValidatorTest {

    @Inject
    lateinit var vardefLabidTokenValidator: VardefLabidTokenValidator<MutableHttpRequest<*>>

    @Test
    fun `request with malformed token`() {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        "not a token",
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth).isNull()
    }

    @Test
    fun `request comes from dapla lab and valid active group supplied`() {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned().parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER, VARIABLE_OWNER, VARIABLE_CREATOR)
    }

    @Test
    fun `request token doesn't contain allowed audience`() {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned(audienceClaim = listOf("blah")).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.roles).doesNotContain(VARIABLE_OWNER)
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
    }

    @ParameterizedTest
    @ValueSource(strings = ["onyxia-api", "dapla-cli"])
    fun `request token contains allowed audience`(allowedAudience: String) {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned(audienceClaim = listOf("blah", allowedAudience)).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER, VARIABLE_OWNER, VARIABLE_CREATOR)
    }

    @Test
    fun `active group not present in token`() {
        assertThrows<InvalidActiveGroupException> {
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned(activeGroup = "unknown-group").parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        }
    }

    @Test
    fun `token is null`() {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
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
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned(daplaGroups = null).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()

        assertThat(auth?.roles).containsExactly(VARIABLE_CONSUMER)
    }


    @Test
    fun `authentication object contains username`() {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned().parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.name).isEqualTo(TEST_USER)
    }

    @Test
    fun `authentication object does not contain username`() {
        val auth =
            Mono
                .from(
                    vardefLabidTokenValidator.validateToken(
                        LabIdTokenHelper.labIdTokenSigned(includeUsername = false).parsedString,
                        HttpRequest.POST("/variable-definitions", ""),
                    ),
                ).block()
        assertThat(auth?.name).isNull()
    }
}
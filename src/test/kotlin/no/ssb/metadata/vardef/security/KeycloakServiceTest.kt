package no.ssb.metadata.vardef.security

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

// @Requires(env = ["integration-test"])
@MicronautTest
class KeycloakServiceTest {
    @Inject
    lateinit var keycloakService: KeycloakService

    @Test
    fun `get keycloak token`() {
        val result = keycloakService.requestAccessToken()
        assertThat(result).isNotBlank()
    }
}

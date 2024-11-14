package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Requires(env = ["integration-test"])
@MicronautTest
class DaplaTeamApiClientTest {
    @Inject
    lateinit var daplaTeamApiClient: DaplaTeamApiClient

    @Inject
    lateinit var keycloakService: KeycloakService

    private fun getAuthHeader(): String {
        val token = keycloakService.requestAccessToken()
        return "Bearer $token"
    }

    @Test
    fun `dapla service request team`() {
        val result = daplaTeamApiClient.fetchTeam("dapla-felles", getAuthHeader())
        assertThat(result).isNotNull
    }

    @Test
    fun `dapla service request group`() {
        val result = daplaTeamApiClient.fetchGroup("dapla-felles-developers", getAuthHeader())
        assertThat(result).isNotNull
    }
}

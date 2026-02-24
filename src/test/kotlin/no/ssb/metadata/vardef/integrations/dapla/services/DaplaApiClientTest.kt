package no.ssb.metadata.vardef.integrations.daplaApi

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.dapla.models.GraphQlRequest
import no.ssb.metadata.vardef.integrations.dapla.services.DaplaApiClient
import no.ssb.metadata.vardef.integrations.dapla.utils.loadQuery
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@Requires(env = ["integration-test"])
@MicronautTest
class DaplaApiClientTest {
    @Inject
    lateinit var daplaApiClient: DaplaApiClient

    @Value("\${dapla.api.token}")
    lateinit var apiToken: String

    private fun getAuthHeader(): String = "Bearer $apiToken"

    @Test
    fun `dapla graphql client request team`() {
        val request =
            GraphQlRequest(
                query = loadQuery("Team.graphql"),
                variables = mapOf("slug" to "dapla-felles"),
            )
        val response = daplaApiClient.fetchTeam(request, getAuthHeader())
        println("TEAM RESPONSE: ${response.body()}")
        assertThat(response.body()?.data?.team).isNotNull
    }

    @Test
    fun `dapla graphql client request group`() {
        val request =
            GraphQlRequest(
                query = loadQuery("Group.graphql"),
                variables = mapOf("name" to "dapla-felles-developers"),
            )
        val result = daplaApiClient.fetchGroup(request, getAuthHeader())
        assertThat(result.body()?.data?.group).isNotNull
    }
}

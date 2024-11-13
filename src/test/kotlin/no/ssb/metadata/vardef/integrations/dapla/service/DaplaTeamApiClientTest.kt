package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
// @Requires(env = ["integration-test"])
class DaplaTeamApiClientTest {
    @Inject
    lateinit var daplaTeamApiClient: DaplaTeamApiClient

    @Test
    fun `dapla team api request`(){
        val result = daplaTeamApiClient.fetchTeam("dapla-felles")
        assertThat(result).isNotNull
    }
}
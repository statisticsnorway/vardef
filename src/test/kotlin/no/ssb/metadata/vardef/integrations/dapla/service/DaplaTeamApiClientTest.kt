package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
// @Requires(env = ["integration-test"])
class DaplaTeamApiClientTest {
    @Inject
    lateinit var daplaTeamApiService: DaplaTeamService

    @Test
    fun `dapla service request`(){
        val result = daplaTeamApiService.getTeam("dapla-felles")
        assertThat(result?.uniformName).isEqualTo("dapla-felles")
    }
}
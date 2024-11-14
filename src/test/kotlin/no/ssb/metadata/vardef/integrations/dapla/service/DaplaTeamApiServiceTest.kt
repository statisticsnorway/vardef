package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest
// @Requires(env = ["integration-test"])
class DaplaTeamApiServiceTest {

    @Inject
    lateinit var daplaTeamApiService: DaplaTeamService

    @Test
    fun `dapla service request`() {
        val result = daplaTeamApiService.getTeam("dapla-felles")
        assertThat(result?.uniformName).isEqualTo("dapla-felles")
    }

    @ParameterizedTest
    @ValueSource(strings = ["dapla-felles", "play-enhjoern-a"])
    fun `valid team names`(teamName: String){
        val result = daplaTeamApiService.isValidTeam(teamName)
        assertThat(result).isTrue
    }
}
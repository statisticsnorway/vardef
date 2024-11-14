package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest
// @Requires(env = ["integration-test"])
class DaplaTeamApiServiceTest {

    @Inject
    lateinit var daplaTeamApiService: DaplaTeamService

    @Test
    fun `get team`() {
        val result = daplaTeamApiService.getTeam("dapla-felles")
        assertThat(result?.uniformName).isEqualTo("dapla-felles")
    }

    @ParameterizedTest
    @MethodSource("isTeamValid")
    fun `check team name with dapla team api`(teamName: String, expectedResult: Boolean){
        assertThat(daplaTeamApiService.isValidTeam(teamName)).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun isTeamValid(): Stream<Arguments> =
            Stream.of(
              arguments(
                  "dapla-felles",
                  true,
              ),
                arguments(
                    "play-enhjoern-a",
                    true,
                ),
                arguments(
                    "play-oh-la-la-bimbom",
                    false,
                ),
                arguments(
                    "",
                    false,
                ),
                arguments(
                    "mimi",
                    false,
                )
            )
    }
}
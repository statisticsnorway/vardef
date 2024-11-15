package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@Requires(env = ["integration-test"])
@MicronautTest
class DaplaTeamApiServiceTest {
    @Inject
    lateinit var daplaTeamApiService: DaplaTeamApiService

    @ParameterizedTest
    @MethodSource("fetchTeam")
    fun `get team from dapla team api`(teamName: String, expectedResult: String?) {
        assertThat(daplaTeamApiService.getTeam(teamName)?.uniformName).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("fetchGroup")
    fun `get group from dapla team api`(groupName: String, expectedResult: String?) {
        assertThat(daplaTeamApiService.getGroup(groupName)?.uniformName).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("isTeamValid")
    fun `check team name with dapla team api`(
        teamName: String,
        expectedResult: Boolean,
    ) {
        assertThat(daplaTeamApiService.isValidTeam(teamName)).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("isGroupValid")
    fun `check group name with dapla team api`(
        groupName: String,
        expectedResult: Boolean,
    ) {
        assertThat(daplaTeamApiService.isValidGroup(groupName)).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun fetchTeam(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "dapla-felles",
                    "dapla-felles",
                ),
                arguments(
                    "play-enhjoern-a",
                    "play-enhjoern-a",
                ),
                arguments(
                    "play-dubi",
                    null,
                ),
                arguments(
                    "",
                    null,
                ),
                arguments(
                    "mimi",
                    null,
                ),
            )
        @JvmStatic
        fun fetchGroup(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "dapla-felles-developers",
                    "dapla-felles-developers",
                ),
                arguments(
                    "play-enhjoern-a-managers",
                    "play-enhjoern-a-managers",
                ),
                arguments(
                    "play-dubi-data-admins",
                    null,
                ),
                arguments(
                    "",
                    null,
                ),
                arguments(
                    "mimi-dev",
                    null,
                ),
            )
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
                ),
            )

        @JvmStatic
        fun isGroupValid(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "dapla-felles-managers",
                    true,
                ),
                arguments(
                    "play-enhjoern-a-developers",
                    true,
                ),
                arguments(
                    "play-enhjoern-a-data-admins",
                    true,
                ),
                arguments(
                    "play-oh-la-la-bimbom-developers",
                    false,
                ),
                arguments(
                    "",
                    false,
                ),
                arguments(
                    "mimi-managers",
                    false,
                ),
            )
    }
}

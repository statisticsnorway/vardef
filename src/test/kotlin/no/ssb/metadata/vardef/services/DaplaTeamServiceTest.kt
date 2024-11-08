package no.ssb.metadata.vardef.services

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import no.ssb.metadata.vardef.models.Owner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest(startApplication = false)
class DaplaTeamServiceTest {
    @ParameterizedTest
    @MethodSource("isDevelopersTestCases")
    fun `test isDevelopers`(
        group: String,
        expected: Boolean,
    ) {
        assertThat(DaplaTeamService.isDevelopers(group)).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("developersGroupTestCases")
    fun `test containsDevelopersGroup`(
        owner: Owner,
        expected: Boolean,
    ) {
        assertThat(DaplaTeamService.containsDevelopersGroup(owner)).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun isDevelopersTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.arguments("my-team-developers", true),
                Arguments.arguments("very-long-string%THAT=contains#special6characters-developers", true),
                Arguments.arguments("-developers", false),
                Arguments.arguments("developers", false),
                Arguments.arguments("my-team-devel", false),
                Arguments.arguments("developers-developers-data-admins", false),
                Arguments.arguments("", false),
                Arguments.arguments("my-team-managers", false),
            )

        @JvmStatic
        fun developersGroupTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.arguments(Owner(team = "my-team", groups = listOf("my-team-developers")), true),
                Arguments.arguments(Owner(team = "my-team", groups = listOf("my-team-developers", "my-team-owner")), true),
                Arguments.arguments(
                    Owner(team = "my-team-developers", groups = listOf("my-team-developers-developers")),
                    true,
                ),
                Arguments.arguments(Owner(team = "my-team-developers", groups = listOf("my-team-developers")), false),
                Arguments.arguments(Owner(team = "my-team", groups = listOf("my-team-owner")), false),
                Arguments.arguments(Owner(team = "", groups = listOf("my-team-developers")), false),
                Arguments.arguments(Owner(team = "my-team", groups = listOf()), false),
                Arguments.arguments(
                    Owner(team = "my-team", groups = listOf("other-team-developers", "yet-another-team-developers")),
                    false,
                ),
            )
    }
}

package no.ssb.metadata.vardef.integrations.dapla

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import jakarta.validation.ConstraintViolation
import no.ssb.metadata.vardef.integrations.dapla.service.StaticDaplaTeamService
import no.ssb.metadata.vardef.integrations.dapla.validators.DaplaTeam
import no.ssb.metadata.vardef.models.Owner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Introspected
data class TestOwnerObject(
    @DaplaTeam
    var team: String,
    var groups: List<String>,
)

@MicronautTest
class ValidateDaplaTeamTest(private val validator: Validator) {
    @Inject
    lateinit var daplaTeamApiStaticService: StaticDaplaTeamService

    @Test
    fun`validate team name`()  {
        assertThat(daplaTeamApiStaticService.isValidTeam("bon")).isFalse()
        assertThat(daplaTeamApiStaticService.isValidTeam("dapla-felles")).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["bon", "babaam"])
    fun `test invalid team name`(team: String)  {
        assertThat(
            validator.validate(
                TestOwnerObject(
                    team,
                    listOf("dapla-felles-developers"),
                ),
            ),
        ).isNotEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["dapla-felles", "play-enhjoern-a"])
    fun `test valid team name`(team: String)  {
        assertThat(
            validator.validate(
                TestOwnerObject(
                    team,
                    listOf("dapla-felles-developers"),
                ),
            ),
        ).isEmpty()
    }

    @Test
    fun `test owner`()  {
        assertThat(validator.validate(Owner("bon", listOf("dapla")))).isNotEmpty()
        val violations: Set<ConstraintViolation<Owner>> = validator.validate(Owner("bon", listOf("dapla")))
        val teamViolation = violations.first { it.propertyPath.toString() == "team" }
        assertThat(teamViolation.message).isEqualTo("Invalid Dapla team")
        assertThat(validator.validate(Owner("dapla-felles", listOf("dapla")))).isEmpty()
    }
}

package no.ssb.metadata.vardef.integrations.dapla

import io.micronaut.context.BeanContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.json.JsonMapper
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.dapla.service.StaticDaplaTeam
import no.ssb.metadata.vardef.integrations.dapla.service.StaticDaplaTeamService
import no.ssb.metadata.vardef.models.Owner
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest
class ValidateDaplaTeamTest(private val validator: Validator) {
    @Inject
    lateinit var daplaTeamApiStaticService: StaticDaplaTeamService

    @Inject
    lateinit var beanContext: BeanContext

    @Inject
    lateinit var jsonMapper: JsonMapper

    @Test
    fun`validate team name`() {
        assertThat(daplaTeamApiStaticService.isValidTeam("bon")).isFalse()
        assertThat(daplaTeamApiStaticService.isValidTeam("dapla-felles")).isTrue()
    }

    @Test
    fun`validate group name`() {
        assertThat(daplaTeamApiStaticService.isValidGroup("bon")).isFalse()
        assertThat(daplaTeamApiStaticService.isValidGroup("dapla-felles-developers")).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["bon", "babaam"])
    fun `test invalid team name`(team: String) {
        assertThat(
            validator.validate(
                Owner(
                    team,
                    listOf("dapla-felles-developers"),
                ),
            ),
        ).isNotEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["dapla-felles", "play-enhjoern-a"])
    fun `test valid team name`(team: String) {
        assertThat(
            validator.validate(
                Owner(
                    team,
                    listOf("dapla-felles-developers"),
                ),
            ),
        ).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "dapla-felles-developers",
            "play-enhjoern-a-developers",
            "pers-skatt-developers",
            "skip-stat-developers",
        ],
    )
    fun `test valid group name`(group: String) {
        assertThat(
            validator.validate(
                Owner(
                    "play-enhjoern-a",
                    listOf(group),
                ),
            ),
        ).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["play-b-dev", "root-manager"])
    fun `test invalid group name`(group: String) {
        assertThat(
            validator.validate(
                Owner(
                    "play-enhjoern-a",
                    listOf(group),
                ),
            ),
        ).isNotEmpty()
    }

    @Test
    fun `test StaticDaplaTeam loads correct data`() {
        val teamA = beanContext.getBean(StaticDaplaTeam::class.java, Qualifiers.byName("dapla-felles"))
        assertEquals("dapla-felles", teamA.uniformName)
    }

    @Test
    fun `test StaticDaplaTeam is valid`() {
        val result = daplaTeamApiStaticService.isValidTeam("dapla-felles")
        assertThat(result).isTrue()
        val result2 = daplaTeamApiStaticService.isValidTeam("min-min")
        assertThat(result2).isFalse()
    }

    @Test
    fun `test JsonMapper injection`() {
        assertNotNull(jsonMapper) // Ensure the JsonMapper bean is injected
    }
}

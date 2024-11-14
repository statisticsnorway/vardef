package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
class StaticDaplaTeamServiceTest {
    @Inject
    lateinit var daplaTeamApiStaticService: StaticDaplaTeamService

    @Test
    fun `test StaticDaplaTeam loads correct data`() {
        val team = daplaTeamApiStaticService.getTeam("dapla-felles")
        assertEquals("dapla-felles", team.uniformName)
    }

    @Test
    fun `test StaticDaplaGroup loads correct data`() {
        val group = daplaTeamApiStaticService.getGroup("dapla-felles-developers")
        assertEquals("dapla-felles-developers", group.uniformName)
    }
}
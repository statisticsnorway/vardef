package no.ssb.metadata.vardef.integrations.dapla.service

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class DaplaTeamApiStaticService : DaplaTeamApiService {
    private val logger = LoggerFactory.getLogger(DaplaTeamApiStaticService::class.java)

    private val teams =
        listOf(
            "play-enhjoern-a",
            "dapla-felles",
            "my-team",
            "pers-skatt",
            "skip-stat",
            "play-foeniks-a",
        )

    override fun isValidTeam(team: String): Boolean {
        logger.info("Checking if team $team is valid")
        return teams.any { it.equals(team, true) }
    }
}

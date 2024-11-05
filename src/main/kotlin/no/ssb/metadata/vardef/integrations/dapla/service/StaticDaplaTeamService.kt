package no.ssb.metadata.vardef.integrations.dapla.service

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import org.slf4j.LoggerFactory

@Singleton
class StaticDaplaTeamService : DaplaTeamService {
    private val logger = LoggerFactory.getLogger(StaticDaplaTeamService::class.java)

    private val teams =
        listOf(
            "play-enhjoern-a",
            "dapla-felles",
            "my-team",
            "pers-skatt",
            "skip-stat",
            "play-foeniks-a",
            "my-oh-my-team",
        )

    private val groups =
        listOf(
            "play-enhjoern-a-developers",
            "play-foeniks-a-developers",
        )

    override fun getTeam(teamName: String): Team? {
        TODO("Not yet implemented")
    }

    override fun isValidTeam(team: String): Boolean {
        logger.info("Checking if team $team is valid")
        return teams.any { it.equals(team, true) }
    }

    override fun isValidGroup(group: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGroup(groupName: String): Group? {
        TODO("Not yet implemented")
    }
}

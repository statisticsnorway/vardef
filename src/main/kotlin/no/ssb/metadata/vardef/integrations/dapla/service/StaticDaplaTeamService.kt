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
            "neighbourhood-dogs",
            "my-team-developers",
            "other-group",
            "my-team-developers",
            "pers-skatt-developers",
            "skip-stat-developers",
            "dapla-felles-developers"
        )

    override fun getTeam(teamName: String): Team? {
        TODO("Not yet implemented")
    }

    override fun isValidTeam(team: String): Boolean {
        logger.info("Checking if team $team is valid")
        return teams.any { it == team }
    }

    override fun isValidGroup(group: String): Boolean {
        logger.info("Checking if team $group is valid")
        return groups.any { it == group }
    }

    override fun getGroup(groupName: String): Group? {
        TODO("Not yet implemented")
    }
}

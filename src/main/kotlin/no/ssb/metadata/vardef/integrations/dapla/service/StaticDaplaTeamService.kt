package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.*
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.json.JsonMapper
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import org.slf4j.LoggerFactory
import java.nio.file.Path

const val DAPLA_TEAM_PROPERTY_NAME = "dapla.teams"

@Singleton
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_TEAM_PROPERTY_NAME)
@EachProperty(DAPLA_TEAM_PROPERTY_NAME)
class StaticDaplaTeam(
    @param:Parameter val uniformName: String,
    @Property(name = "dapla.static-data-path")
    private val path: Path,
    jsonMapper: JsonMapper,
) {
    var team: Team

    init {
        val resourcePath = path.resolve("$uniformName.json").toString()
        val resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath)
        if (resource == null) {
            throw RuntimeException("Resource not found: $resourcePath")
        } else {
            team = jsonMapper.readValue(resource.readText(), Team::class.java)
        }
    }

}

@Primary
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_TEAM_PROPERTY_NAME)
@Singleton
class StaticDaplaTeamService(private val beanContext: BeanContext): DaplaTeamService {
    private val logger = LoggerFactory.getLogger(StaticDaplaTeamService::class.java)

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
            "dapla-felles-developers",
            "my-oh-my-team-developers",
        )

    override fun getTeam(teamName: String): Team {
        val team: StaticDaplaTeam =
            beanContext.getBean(StaticDaplaTeam::class.java, Qualifiers.byName(teamName))
        return Team(team.toString())
    }

    override fun isValidTeam(team: String): Boolean =
        try {

            logger.info("Checking if team '$team' is valid")

            // Log the available beans in the context to help diagnose if the bean exists
            logger.info("Available beans for StaticDaplaTeam: ${beanContext.getBeansOfType(StaticDaplaTeam::class.java)}")

            // Try to get the bean
            val teamBean = beanContext.getBean(StaticDaplaTeam::class.java, Qualifiers.byName(team))

            // Log after successfully getting the bean
            logger.info("Team '$team' is valid: $teamBean")
            true
        } catch (e: Exception) {
            logger.error("Error retrieving StaticDaplaTeam for team '$team'", e)
            false
    }

    override fun isValidGroup(group: String): Boolean {
        logger.info("Checking if group $group is valid")
        return groups.any { it == group }
    }

    override fun getGroup(groupName: String): Group? {
        TODO("Not yet implemented")
    }
}

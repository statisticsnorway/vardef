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

const val DAPLA_TEAM_PROPERTY_NAME = "dapla-teams.teams"
const val DAPLA_GROUP_PROPERTY_NAME = "dapla-groups.groups"
const val DAPLA_PROPERTY = "dapla"

@Singleton
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_TEAM_PROPERTY_NAME)
@EachProperty(DAPLA_TEAM_PROPERTY_NAME)
class StaticDaplaTeam(
    @param:Parameter val uniformName: String,
    @Property(name = "dapla-teams.static-data-path")
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

@Singleton
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_GROUP_PROPERTY_NAME)
@EachProperty(DAPLA_GROUP_PROPERTY_NAME)
class StaticDaplaGroup(
    @param:Parameter val uniformName: String,
    @Property(name = "dapla-groups.static-data-path")
    private val path: Path,
    jsonMapper: JsonMapper,
) {
    var group: Group

    init {
        val resourcePath = path.resolve("$uniformName.json").toString()
        val resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath)
        if (resource == null) {
            throw RuntimeException("Resource not found: $resourcePath")
        } else {
            group = jsonMapper.readValue(resource.readText(), Group::class.java)
        }
    }
}

@Primary
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_PROPERTY)
@Singleton
class StaticDaplaTeamService(private val beanContext: BeanContext) : DaplaTeamService {
    private val logger = LoggerFactory.getLogger(StaticDaplaTeamService::class.java)

    override fun getTeam(teamName: String): Team {
        val team: StaticDaplaTeam =
            beanContext.getBean(StaticDaplaTeam::class.java, Qualifiers.byName(teamName))
        return Team(team.toString())
    }

    override fun isValidTeam(team: String): Boolean =
        try {
            logger.info("Checking if team '$team' is valid")

            // Log the available beans in the context
            logger.info("Available beans for StaticDaplaTeam: ${beanContext.getBeansOfType(StaticDaplaTeam::class.java)}")

            // Try to get the bean
            val teamBean = getTeam(team)
            // val teamBean = beanContext.getBean(StaticDaplaTeam::class.java, Qualifiers.byName(team))

            // Log after successfully getting the bean
            logger.info("Team '$team' is valid: $teamBean")
            true
        } catch (e: Exception) {
            logger.error("Error retrieving StaticDaplaTeam for team '$team'", e)
            false
        }

    override fun isValidGroup(group: String): Boolean =
        // logger.info("Checking if group $group is valid")
        // return groups.any { it == group }
        try {
            logger.info("Checking if group '$group' is valid")

            // Log the available beans in the context
            logger.info("Available beans for StaticDaplaGroup: ${beanContext.getBeansOfType(StaticDaplaGroup::class.java)}")

            // Try to get the bean
            val groupBean = getGroup(group)
            // val groupBean = beanContext.getBean(StaticDaplaGroup::class.java, Qualifiers.byName(group))

            // Log after successfully getting the bean
            logger.info("Group '$group' is valid: $groupBean")
            true
        } catch (e: Exception) {
            logger.error("Error retrieving StaticDaplaGroup for group '$group'", e)
            false
        }

    override fun getGroup(groupName: String): Group {
        val group: StaticDaplaGroup =
            beanContext.getBean(StaticDaplaGroup::class.java, Qualifiers.byName(groupName))
        return Group(group.toString())
    }
}

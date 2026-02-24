package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.*
import io.micronaut.core.type.Argument
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.json.JsonMapper
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.GraphQlResponse
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.GroupData
import no.ssb.metadata.vardef.integrations.dapla.models.Section
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.integrations.dapla.models.TeamData
import org.slf4j.LoggerFactory
import java.nio.file.Path

const val DAPLA_TEAM_PROPERTY_NAME = "dapla-teams.teams"
const val DAPLA_GROUP_PROPERTY_NAME = "dapla-groups.groups"
const val DAPLA_PROPERTY = "dapla"

/**
 * StaticDaplaTeam is a testing implementation of a team data provider that loads team data from
 * static JSON files. This class is activated in the test environment (excluding integration tests)
 * and reads team configurations from JSON files based on the team name.
 *
 * @property slug The slug used to identify and load the team data.
 * @property sectionCode The section code associated with the team.
 * @property path The path to the directory where static team data JSON files are stored.
 * @property team The `Team` object populated from the JSON data.
 * @throws RuntimeException if the specified resource JSON file is not found.
 */
@Singleton
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_TEAM_PROPERTY_NAME)
@EachProperty(DAPLA_TEAM_PROPERTY_NAME)
class StaticDaplaTeam(
    @param:Parameter val slug: String,
    @param:Parameter val sectionCode: String,
    @param:Parameter val sectionName: String,
    @param:Property(name = "dapla-teams.static-data-path")
    private val path: Path,
    private val jsonMapper: JsonMapper,
) {
    val team: Team = loadTeamData()

    private fun loadTeamData(): Team {
        val resourcePath = "$slug.json"
        val resource =
            Thread.currentThread().contextClassLoader.getResource(path.resolve(resourcePath).toString())
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        @Suppress("UNCHECKED_CAST")
        val response =
            jsonMapper.readValue(
                resource.readText(),
                Argument.of(GraphQlResponse::class.java, TeamData::class.java),
            ) as GraphQlResponse<TeamData>
        return response.data?.team
            ?: throw IllegalArgumentException("Team not found inside data for $resourcePath")
    }
}

/**
 * StaticDaplaGroup is a testing implementation of a group data provider that loads group data
 * from static JSON files. This class is activated in the test environment (excluding integration tests)
 * and reads group configurations from JSON files based on the group name.
 *
 * @property name The name used to identify and load the group data.
 * @property path The path to the directory where static group data JSON files are stored.
 * @property group The `Group` object populated from the JSON data.
 * @throws RuntimeException if the specified resource JSON file is not found.
 */
@Singleton
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_GROUP_PROPERTY_NAME)
@EachProperty(DAPLA_GROUP_PROPERTY_NAME)
class StaticDaplaGroup(
    @param:Parameter val name: String,
    @param:Property(name = "dapla-groups.static-data-path")
    private val path: Path,
    private val jsonMapper: JsonMapper,
) {
    val group: Group = loadGroupData()

    private fun loadGroupData(): Group {
        val resourcePath = "$name.json"
        val resource =
            Thread.currentThread().contextClassLoader.getResource(path.resolve(resourcePath).toString())
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

        @Suppress("UNCHECKED_CAST")
        val response =
            jsonMapper.readValue(
                resource.readText(),
                Argument.of(GraphQlResponse::class.java, GroupData::class.java),
            ) as GraphQlResponse<GroupData>
        return response.data?.group
            ?: throw IllegalArgumentException("Group not found inside data for $resourcePath")
    }
}

/**
 * StaticDaplaTeamService is a testing service that provides `Team` and `Group` data based on
 * static JSON files. This class is activated in the test environment (excluding integration tests)
 * and retrieves team and group beans configured by name. It validates the existence of teams
 * and groups and logs relevant information for testing purposes.
 *
 * @property beanContext The bean context used to locate and retrieve `StaticDaplaTeam` and
 *                       `StaticDaplaGroup` beans by name.
 */
@Primary
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_PROPERTY)
@Singleton
class StaticDaplaTeamService(
    private val beanContext: BeanContext,
) : DaplaTeamService {
    private val logger = LoggerFactory.getLogger(StaticDaplaTeamService::class.java)

    /**
     * Retrieves a `Team` instance based on the specified team name.
     *
     * @param teamName The name of the team to retrieve.
     * @return The `Team` object corresponding to the given team name, or null if not found.
     */
    override fun getTeam(teamName: String): Team? =
        runCatching {
            val staticTeam = beanContext.getBean(StaticDaplaTeam::class.java, Qualifiers.byName(teamName))
            Team(
                slug = staticTeam.slug,
                section = Section(code = staticTeam.sectionCode, name = staticTeam.sectionName),
            )
        }.onFailure { e ->
            logger.error("Error fetching static team with name '$teamName': ${e.message}", e)
        }.getOrNull()

    /**
     * Retrieves a `Group` instance based on the specified group name.
     *
     * @param groupName The name of the group to retrieve.
     * @return The `Group` object corresponding to the given group name, or null if not found.
     */
    override fun getGroup(groupName: String): Group? =
        runCatching {
            val staticGroup = beanContext.getBean(StaticDaplaGroup::class.java, Qualifiers.byName(groupName))
            Group(
                name = staticGroup.name,
            )
        }.onFailure { e ->
            logger.error("Error fetching static group with name '$groupName': ${e.message}", e)
        }.getOrNull()

    /**
     * Checks if a team with the specified name exists.
     *
     * @param teamName The name of the team to validate.
     * @return `true` if the team exists, `false` otherwise.
     */
    override fun isValidTeam(teamName: String): Boolean = getTeam(teamName) != null

    /**
     * Checks if a group with the specified name exists.
     *
     * @param groupName The name of the group to validate.
     * @return `true` if the group exists, `false` otherwise.
     */
    override fun isValidGroup(groupName: String): Boolean = getGroup(groupName) != null
}

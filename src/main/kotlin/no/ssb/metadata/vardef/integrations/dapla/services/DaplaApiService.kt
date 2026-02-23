package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.GraphQlRequest
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.integrations.dapla.utils.loadQuery
import org.slf4j.LoggerFactory

@Singleton
open class DaplaApiService(
    private val daplaGraphQlClient: DaplaApiClient,
    @Value("\${dapla.api.token}") private val apiToken: String,
) : DaplaTeamService {
    private val logger = LoggerFactory.getLogger(DaplaTeamService::class.java)

    private fun getAuthToken(): String = "Bearer $apiToken"

    override fun getTeam(teamName: String): Team? =
        runCatching {
            val request =
                GraphQlRequest(
                    query = loadQuery("Team.graphql"),
                    variables = mapOf("slug" to teamName),
                )

            daplaGraphQlClient
                .fetchTeam(request, getAuthToken())
                .body()
                ?.data
                ?.team
        }.onFailure { e ->
            logger.error("Error fetching team with slug '$teamName': ${e.message}", e)
        }.getOrNull()

    override fun isValidTeam(teamName: String): Boolean = getTeam(teamName) != null

    override fun getGroup(groupName: String): Group? =
        runCatching {
            val queryTemplate = loadQuery("Group.graphql")
            val request =
                GraphQlRequest(
                    query = queryTemplate,
                    variables = mapOf("name" to groupName),
                )

            daplaGraphQlClient
                .fetchGroup(request, getAuthToken())
                .body()
                ?.data
                ?.group
        }.onFailure { e ->
            logger.error("Error fetching group with name '$groupName': ${e.message}", e)
        }.getOrNull()

    override fun isValidGroup(groupName: String): Boolean = getGroup(groupName) != null
}

package no.ssb.metadata.vardef.integrations.dapla.services

import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import org.slf4j.LoggerFactory

@Singleton
open class DaplaTeamApiService(private val daplaTeamApiClient: DaplaTeamApiClient) : DaplaTeamService {
    private val logger = LoggerFactory.getLogger(DaplaTeamService::class.java)

    @Inject
    lateinit var keycloakService: KeycloakService

    private fun getAuthToken(): String {
        val token = keycloakService.requestAccessToken()
        return requireNotNull("Bearer $token") {
            "Authorization token is missing"
        }
    }

    override fun getTeam(teamName: String): Team? {
        try {
            val team = daplaTeamApiClient.fetchTeam(teamName, getAuthToken())
            return team.body()
        } catch (e: Exception) {
            logger.error("Error fetching team $teamName", e)
            return null
        }
    }

    override fun isValidTeam(teamName: String): Boolean = getTeam(teamName) != null

    override fun getGroup(groupName: String): Group? {
        try {
            val group = daplaTeamApiClient.fetchGroup(groupName, getAuthToken())
            return group.body()
        } catch (e: Exception) {
            logger.error("Error fetching group $groupName", e)
            return null
        }
    }

    override fun isValidGroup(groupName: String): Boolean = getGroup(groupName) != null
}

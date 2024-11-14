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
        return runCatching {
            daplaTeamApiClient.fetchTeam(teamName, getAuthToken()).body()
        }.onFailure { e ->
            logger.error("Error fetching team with name '$teamName': ${e.message}", e)
        }.getOrNull()
    }

    override fun isValidTeam(teamName: String): Boolean = getTeam(teamName) != null

    override fun getGroup(groupName: String): Group? {
        return runCatching {
            daplaTeamApiClient.fetchGroup(groupName, getAuthToken()).body()
        }.onFailure { e ->
            logger.error("Error fetching group with name '$groupName': ${e.message}", e)
        }.getOrNull()
    }

    override fun isValidGroup(groupName: String): Boolean = getGroup(groupName) != null
}

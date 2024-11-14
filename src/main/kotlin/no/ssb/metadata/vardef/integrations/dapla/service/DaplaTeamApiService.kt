package no.ssb.metadata.vardef.integrations.dapla.service

import jakarta.inject.Inject
import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team
import no.ssb.metadata.vardef.security.KeycloakService
import org.slf4j.LoggerFactory

@Singleton
open class DaplaTeamApiService(private val daplaTeamApiClient: DaplaTeamApiClient) : DaplaTeamService {
    private val logger = LoggerFactory.getLogger(DaplaTeamService::class.java)

    @Inject
    lateinit var keycloakService: KeycloakService

    override fun getTeam(teamName: String): Team? {
        val token = keycloakService.requestAccessToken() // get the access token from Keycloak
        val authorizationHeader = "Bearer $token"
        try {
            val team = daplaTeamApiClient.fetchTeam(teamName, authorizationHeader)
            return team.body()
        } catch (e: Exception) {
            logger.info("Can not fetch $teamName: ${e.message}")
            return null
        }
    }

    override fun isValidTeam(team: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isValidGroup(group: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGroup(groupName: String): Group? {
        TODO("Not yet implemented")
    }
}

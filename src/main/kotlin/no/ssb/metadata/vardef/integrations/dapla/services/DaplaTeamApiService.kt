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

    /**
     * Retrieves an authorization token for accessing Dapla Team Api.
     *
     * This function requests an access token from the `keycloakService` and formats it as a Bearer token.
     * If the token is `null`, an `IllegalArgumentException` is thrown with a message indicating that the
     * authorization token is missing.
     *
     * @return A `String` containing the Bearer token for authorization.
     * @throws IllegalArgumentException if the token cannot be retrieved.
     */
    private fun getAuthToken(): String {
        val token = keycloakService.requestAccessToken()
        return requireNotNull("Bearer $token") {
            "Authorization token is missing"
        }
    }

    /**
     * Retrieves team details for the specified team name from Dapla Team Api.
     *
     * This function attempts to fetch a `Team` object using the provided
     * `teamName` and an authorization token.
     * If the fetch operation fails `null` is returned.
     *
     * @param teamName The name of the team to be fetched.
     * @return The `Team` object if found, or `null` if an error occurs or if the team is not found.
     */
    override fun getTeam(teamName: String): Team? {
        return runCatching {
            daplaTeamApiClient.fetchTeam(teamName, getAuthToken()).body()
        }.onFailure { e ->
            logger.error("Error fetching team with name '$teamName': ${e.message}", e)
        }.getOrNull()
    }

    /**
     * Checks if a team with the specified name exists and is valid.
     *
     * This function attempts to retrieve a `Team` object for the provided `teamName`.
     * It returns `true` if the team exists (i.e., `getTeam` returns a non-null value),
     * and `false` otherwise.
     *
     * @param teamName The name of the team to validate.
     * @return `true` if the team exists, `false` if the team does not exist or cannot be retrieved.
     */
    override fun isValidTeam(teamName: String): Boolean = getTeam(teamName) != null

    /**
     * Retrieves group details for the specified group name from Dapla Team Api.
     *
     * This function attempts to fetch a `Group` object using the provided
     * `groupName` and an authorization token.
     * If the fetch operation fails `null` is returned.
     *
     * @param groupName The name of the group to be fetched.
     * @return The `Group` object if found, or `null` if an error occurs or if the group is not found.
     */
    override fun getGroup(groupName: String): Group? {
        return runCatching {
            daplaTeamApiClient.fetchGroup(groupName, getAuthToken()).body()
        }.onFailure { e ->
            logger.error("Error fetching group with name '$groupName': ${e.message}", e)
        }.getOrNull()
    }

    /**
     * Checks if a group with the specified name exists and is valid.
     *
     * This function attempts to retrieve a `Group` object for the provided `groupName`.
     * It returns `true` if the team exists (i.e., `getTeam` returns a non-null value),
     * and `false` otherwise.
     *
     * @param groupName The name of the group to validate.
     * @return `true` if the group exists, `false` if the group does not exist or cannot be retrieved.
     */
    override fun isValidGroup(groupName: String): Boolean = getGroup(groupName) != null
}

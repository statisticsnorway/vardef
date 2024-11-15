package no.ssb.metadata.vardef.integrations.dapla.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.client.exceptions.NoHostException
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * Service for interacting with Keycloak to request access tokens.
 *
 * The `KeycloakService` class is responsible for obtaining an OAuth2 access token from a Keycloak server.
 * It constructs and sends an HTTP POST request to the token endpoint using client credentials, which are
 * configured via properties. The retrieved token is used for authenticating subsequent requests to secured APIs.
 *
 * @property httpClient The HTTP client used for making requests to Keycloak.
 * @property keycloakUrl The base URL for the Keycloak server
 * @property clientId The client ID used for authentication with Keycloak.
 * @property clientSecret The client secret used for authentication with Keycloak.
 */
@Singleton
class KeycloakService(
    @Client private val httpClient: HttpClient,
) {
    private val logger = LoggerFactory.getLogger(KeycloakService::class.java)

    @Property(name = "keycloak.url")
    lateinit var keycloakUrl: String

    @Property(name = "keycloak.clientId")
    lateinit var clientId: String

    @Property(name = "keycloak.clientSecret")
    lateinit var clientSecret: String

    /**
     * Requests an access token from Keycloak.
     *
     * This function constructs a request to Keycloak's token endpoint using the client credentials.
     * It returns the access token if successful, or `null` if the request fails.
     *
     * @return The access token as a `String`, or `null` if the token cannot be retrieved.
     */
    fun requestAccessToken(): String? {
        val tokenEndpoint = "$keycloakUrl/realms/ssb/protocol/openid-connect/token"
        val formData =
            mapOf(
                "grant_type" to "client_credentials",
                "client_id" to clientId,
                "client_secret" to clientSecret,
            )

        val request =
            HttpRequest.POST(tokenEndpoint, formData)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)

        return runCatching {
            httpClient.toBlocking().retrieve(request, AccessTokenResponse::class.java).accessToken
        }.onFailure { e ->
            if (e is HttpClientResponseException) {
                logger.error("Error fetching token: ${e.status} - ${e.message}", e)
            }
            if (e is NoHostException) {
                logger.error("Error connecting host: ${e.message}", e)
            } else
                {
                    logger.error("Unexpected error: ${e.message}", e)
                }
        }.getOrNull()
    }

    /**
     * Data class representing the access token response from Keycloak.
     *
     * This class maps the JSON response from Keycloak, containing the access token.
     *
     * @property accessToken The OAuth2 access token returned by Keycloak.
     */
    @Serdeable(naming = SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AccessTokenResponse(
        @JsonProperty("access_token") val accessToken: String,
    )
}

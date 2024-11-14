package no.ssb.metadata.vardef.integrations.dapla.services

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class KeycloakService(@Client private val httpClient: HttpClient) {
    private val logger = LoggerFactory.getLogger(KeycloakService::class.java)

    @Property(name = "keycloak.url")
    lateinit var keycloakUrl: String

    @Property(name = "keycloak.clientId")
    lateinit var clientId: String

    @Property(name = "keycloak.clientSecret")
    lateinit var clientSecret: String

    fun requestAccessToken(): String? {
        val tokenEndpoint = "$keycloakUrl/realms/ssb/protocol/openid-connect/token"
        logger.info("Endpoint $tokenEndpoint")
        logger.info("Client id $clientId")

        val formData =
            mapOf(
                "grant_type" to "client_credentials",
                "client_id" to clientId,
                "client_secret" to clientSecret,
            )

        val request =
            HttpRequest.POST(tokenEndpoint, formData)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)

        return try {
            val response = httpClient.toBlocking().retrieve(request, AccessTokenResponse::class.java)
            logger.info("Response: $response")
            response.accessToken
        } catch (e: HttpClientResponseException) {
            println("Error fetching token: ${e.status} - ${e.message}")
            null
        }
    }

    @Serdeable(naming = SnakeCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AccessTokenResponse(
        @JsonProperty("access_token") val accessToken: String,
    )
}

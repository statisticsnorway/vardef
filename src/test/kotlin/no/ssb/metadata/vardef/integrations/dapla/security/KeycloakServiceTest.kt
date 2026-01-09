package no.ssb.metadata.vardef.integrations.dapla.security

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.TestLogAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@Requires(env = ["integration-test"])
@MicronautTest
class KeycloakServiceTest {
    @Inject
    lateinit var keycloakService: KeycloakService

    private val testLogAppender = TestLogAppender()

    private lateinit var originalKeyCloakClientId: String
    private lateinit var originalKeyCloakClientSecret: String

    // Unpack wrapped logger
    private val context = LoggerFactory.getILoggerFactory() as LoggerContext
    private val logger = context.getLogger(KeycloakService::class.java.name) as Logger

    @BeforeEach
    fun setup() {
        // Save original KeycloakService properties to ensure reset to valid values
        originalKeyCloakClientId = keycloakService.clientId
        originalKeyCloakClientSecret = keycloakService.clientSecret
        // start collecting logger messages
        testLogAppender.start()
        logger.addAppender(testLogAppender)
    }

    @BeforeEach
    fun invalidateCaches(): Unit = keycloakService.invalidateCaches()

    @AfterEach
    fun cleanup() {
        // Stop and reset logger testlogAppender
        println("cleaning up")
        testLogAppender.stop()
        testLogAppender.reset()
        // Reset KeycloakService properties
        keycloakService.clientId = originalKeyCloakClientId
        keycloakService.clientSecret = originalKeyCloakClientSecret
    }

    @Test
    fun `get keycloak token`() {
        val result = keycloakService.requestAccessToken()
        testLogAppender.getLoggedMessages().isEmpty()
        assertThat(result).isNotBlank()
    }

    @Test
    fun `incorrect client`() {
        keycloakService.clientId = "test-client-id"
        val result = keycloakService.requestAccessToken()
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("Client 'keycloak': Unauthorized")
            },
        ).isTrue
        assertThat(result).isNull()
    }

    @Test
    fun `incorrect secret`() {
        keycloakService.clientSecret = "jjjj"
        val result = keycloakService.requestAccessToken()
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("Client 'keycloak': Unauthorized")
            },
        ).isTrue
        assertThat(result).isNull()
    }

    @Test
    fun `token is cached`() {
        val token1 = keycloakService.requestAccessToken()
        assertThat(token1).isNotBlank()
        val token2 = keycloakService.requestAccessToken()

        assertThat(token1).isEqualTo(token2)
    }

    @Test
    fun `cached token when credentials change`() {
        val token1 = keycloakService.requestAccessToken()
        testLogAppender.getLoggedMessages().isEmpty()
        assertThat(token1).isNotBlank()

        // Invalid client id
        keycloakService.clientId = "test-client-id"
        keycloakService.clientSecret = "jjjj"
        val token2 = keycloakService.requestAccessToken()
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("Client 'keycloak': Unauthorized")
            },
        ).isTrue
        assertThat(token2).isNull()
    }
}

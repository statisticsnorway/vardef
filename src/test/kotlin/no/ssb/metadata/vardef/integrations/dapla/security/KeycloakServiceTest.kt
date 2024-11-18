package no.ssb.metadata.vardef.integrations.dapla.security

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.TestLogAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory

@MicronautTest
class KeycloakServiceTest {
    @Inject
    lateinit var keycloakService: KeycloakService

    private val testLogAppender = TestLogAppender()

    @Property(name = "keycloak.url")
    lateinit var keycloakUrl: String

    @Property(name = "keycloak.clientId")
    lateinit var clientId: String

    @Property(name = "keycloak.clientSecret")
    lateinit var clientSecret: String

    private lateinit var originalKeyCloakUrl: String
    private lateinit var originalKeyCloakClientId: String
    private lateinit var originalKeyCloakClientSecret: String

    // Unpack wrapped logger
    private val context = LoggerFactory.getILoggerFactory() as LoggerContext
    private val logger = context.getLogger(KeycloakService::class.java.name) as Logger

    @BeforeEach
    fun setup() {
        // Save original KeycloakService properties to ensure reset to valid values
        originalKeyCloakUrl = keycloakService.keycloakUrl
        originalKeyCloakClientId = keycloakService.clientId
        originalKeyCloakClientSecret = keycloakService.clientSecret
        // start collecting logger messages
        testLogAppender.start()
        logger.addAppender(testLogAppender)
    }

    @AfterEach
    fun cleanup() {
        // Stop and reset logger testlogAppender
        println("cleaning up")
        testLogAppender.stop()
        testLogAppender.reset()
        // Reset KeycloakService properties
        keycloakService.keycloakUrl = originalKeyCloakUrl
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
                it.formattedMessage.contains("UNAUTHORIZED - Unauthorized")
            },
        ).isTrue()
        assertThat(result).isNull()
    }

    @Test
    fun `incorrect url`() {
        keycloakService.keycloakUrl = "www.example.com"
        val result = keycloakService.requestAccessToken()
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("Request URI specifies no host")
            },
        ).isTrue()
        assertThat(result).isNull()
    }

    @Test
    fun `incorrect secret`() {
        keycloakService.clientSecret = "jjjj"
        val result = keycloakService.requestAccessToken()
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("UNAUTHORIZED - Unauthorized")
            },
        ).isTrue()
        assertThat(result).isNull()
    }
}

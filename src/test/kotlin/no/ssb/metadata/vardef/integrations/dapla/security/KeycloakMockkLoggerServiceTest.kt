package no.ssb.metadata.vardef.integrations.dapla.security

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.micronaut.context.annotation.Property
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@MockK
class KeycloakMockkLoggerServiceTest {
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

    // Create a mock for KeycloakService
    private val keycloakServiceMock = mockk<KeycloakService>(relaxed = true)

    @BeforeEach
    fun setup() {
        // Mock KeycloakService properties
        every { keycloakServiceMock.keycloakUrl } returns "http://mock-keycloak-url"
        every { keycloakServiceMock.clientId } returns "mock-client-id"
        every { keycloakServiceMock.clientSecret } returns "mock-client-secret"

        // Save original KeycloakService properties to ensure reset to valid values
        originalKeyCloakUrl = keycloakServiceMock.keycloakUrl
        originalKeyCloakClientId = keycloakServiceMock.clientId
        originalKeyCloakClientSecret = keycloakServiceMock.clientSecret

        // Start collecting logger messages
        testLogAppender.start()
        logger.addAppender(testLogAppender)
    }

    @AfterEach
    fun cleanup() {
        // Stop and reset logger testLogAppender
        println("cleaning up")
        testLogAppender.stop()
        testLogAppender.reset()

        // Verify and reset KeycloakService mock
        clearMocks(keycloakServiceMock) // Clear any interactions and reset

        // Reset KeycloakService properties if needed
        every { keycloakServiceMock.keycloakUrl } returns originalKeyCloakUrl
        every { keycloakServiceMock.clientId } returns originalKeyCloakClientId
        every { keycloakServiceMock.clientSecret } returns originalKeyCloakClientSecret
    }

    @Test
    fun `get keycloak token`() {
        val url = keycloakServiceMock.keycloakUrl
        assertEquals("http://mock-keycloak-url", url)

        val result = keycloakServiceMock.requestAccessToken()
        assertThat(result).isNotNull
        assertThat(testLogAppender.getLoggedMessages().isEmpty())
    }
}

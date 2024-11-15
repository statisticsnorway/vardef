package no.ssb.metadata.vardef.integrations.dapla.security

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory

/**
 * A custom appender for logging events used in testing scenarios.
 *
 * This class extends [AppenderBase] and implements a simple in-memory appender
 * that collects log messages for inspection. It allows you to capture logs generated
 * during tests and later retrieve or clear them for validation purposes.
 *
 * @constructor Creates a [TestLogAppender] instance.
 */
class TestLogAppender : AppenderBase<ILoggingEvent>() {
    private val logMessages = mutableListOf<ILoggingEvent>()

    override fun append(eventObject: ILoggingEvent?) {
        if (eventObject != null) {
            logMessages.add(eventObject)
        }
    }

    fun getLoggedMessages(): List<ILoggingEvent> = logMessages

    fun reset() = logMessages.clear()
}

@Requires(env = ["integration-test"])
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
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("UNAUTHORIZED - Unauthorized")
            },
        ).isFalse()
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

package no.ssb.metadata.vardef.integrations.dapla.services

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.utils.TestLogAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

@MicronautTest
@Requires(env = ["test"], notEnv = ["integration-test"], property = DAPLA_PROPERTY)
class StaticDaplaTeamServiceTest {
    @Inject
    lateinit var daplaTeamApiStaticService: StaticDaplaTeamService

    private val testLogAppender = TestLogAppender()

    // Unpack wrapped logger
    private val context = LoggerFactory.getILoggerFactory() as LoggerContext
    private val logger = context.getLogger(StaticDaplaTeamService::class.java.name) as Logger

    @BeforeEach
    fun setup() {
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
    }

    @Test
    fun `test StaticDaplaTeam loads correct data`() {
        val team = daplaTeamApiStaticService.getTeam("dapla-felles")
        assertThat(testLogAppender.getLoggedMessages()).isEmpty()
        assertEquals("dapla-felles", team?.uniformName)
    }

    @Test
    fun `team not found`() {
        val result = daplaTeamApiStaticService.getTeam("dubi")
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("No bean of type")
            },
        ).isTrue()
        assertThat(result).isNull()
    }

    @Test
    fun `team not valid`() {
        val result = daplaTeamApiStaticService.isValidTeam("dubi")
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("No bean of type")
            },
        ).isTrue()
        assertThat(result).isFalse()
    }

    @Test
    fun `test StaticDaplaGroup loads correct data`() {
        val group = daplaTeamApiStaticService.getGroup("dapla-felles-developers")
        assertThat(
            testLogAppender.getLoggedMessages().any {
                it.formattedMessage.contains("No bean of type")
            },
        ).isFalse()
        assertEquals("dapla-felles-developers", group?.uniformName)
    }
}

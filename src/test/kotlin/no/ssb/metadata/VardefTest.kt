package no.ssb.metadata

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest(transactional = false)
class VardefTest {
    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Test
    fun testItWorks() {
        assertThat(application.isRunning).isTrue()
    }

    @Test
    fun testWorkflowTest() {
        val testBool = true
        assertThat(testBool).withFailMessage("True can never be false", testBool, false).isTrue()
    }

    @Test
    fun testInCorrectLinting() {
        val num = 500
            assertThat(num).isEqualTo(500)
    }

    @Test
    fun testTrueFalse() {
        assertThat(false).isFalse()
    }
}

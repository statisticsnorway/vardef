package no.ssb.metadata.klass

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassTest {
    @Inject
    lateinit var application: EmbeddedApplication<*>

    @Test
    fun testItWorks() {
        org.junit.jupiter.api.Assertions.assertTrue(application.isRunning)
    }

    @Test
    fun testKlass() {
        Assertions.assertThat(true).isTrue()
    }
}

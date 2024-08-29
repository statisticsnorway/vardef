package no.ssb.metadata.vardef.extensions

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

@MicronautTest(startApplication = false)
class LocalDateExtensionsTest {
    @Test
    fun `local date a is equal or before local date b`() {
        val a = LocalDate.now()
        val b = LocalDate.now().plusDays(1L)

        Assertions.assertTrue(a.isEqualOrBefore(a))
        Assertions.assertTrue(a.isEqualOrBefore(b))
    }

    @Test
    fun `local date b is equal or after local date a`() {
        val a = LocalDate.now()
        val b = LocalDate.now().plusDays(1L)

        Assertions.assertTrue(b.isEqualOrAfter(b))
        Assertions.assertTrue(b.isEqualOrAfter(a))
    }
}

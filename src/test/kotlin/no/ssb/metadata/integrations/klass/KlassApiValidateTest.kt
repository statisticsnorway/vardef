package no.ssb.metadata.integrations.klass

import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiValidateTest {
    @Inject
    lateinit var validator: Validator

    @Test
    fun `klass code validation`() {
        val classItem = ClassificationItem(null, 123, null, null, null)
        assertThat(classItem).isNotNull
    }
}

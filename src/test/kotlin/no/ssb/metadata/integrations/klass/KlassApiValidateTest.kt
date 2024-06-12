package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.KlassValidationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiValidateTest {

    @Inject
    lateinit var validator: Validator

    @Test
    fun `klass code validation`() {
        val classItem = ClassificationItem(null,123,null,null,null)
        assertThat(classItem).isNotNull

    }
}
package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassId
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassIdUtil.isValidId
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest(startApplication = false)
class KlassApiValidatorTest(private val validator: Validator) {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "19", "22", "7", "33", "8",
        ],
    )
    fun `invalid klass id`(code: String) {
        assertThat(isValidId(code)).isFalse()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "1", "2", "13", "14",
        ],
    )
    fun `valid klass id`(code: String) {
        assertThat(isValidId(code)).isTrue()
    }

    @Test
    fun `klass id validation illegal id`() {
        val testObject = TestObject("11")
        val violations = validator.validate(testObject)
        assertThat(violations).isNotEmpty()
    }

    @Test
    fun `klass id validation legal id`() {
        assertThat(validator.validate(TestObject("14"))).isEmpty()
    }
}

@Introspected
data class TestObject(
    @KlassId var id: String? = null,
)

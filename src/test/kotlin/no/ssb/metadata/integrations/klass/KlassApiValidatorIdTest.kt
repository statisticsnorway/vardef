package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.KlassId
import no.ssb.metadata.vardef.integrations.klass.validators.klassid.ValidKlassId.isValidId
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/*
TODO: Edit tests when caching object is implemented
 */
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
        assertTrue(isValidId(code))
    }

    @Test
    fun `null klass id`() {
        assertFalse(isValidId(null))
    }

    @Test
    fun `empty klass id`() {
        assertFalse(isValidId(""))
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

    @Test
    fun `klass id validation null`() {
        assertThat(validator.validate(TestObject(null))).isNotEmpty()
    }

    @Test
    fun `empty klass code`() {
        val result = validator.validate(TestObject(""))
        assertThat(result).isNotEmpty()
        assertThat(result.elementAt(0).message).isEqualTo("Invalid klass id ()")
    }
}

/*
TODO: Remove/replace when caching object is implemented
 */
@Introspected
data class TestObject(
    @KlassId var id: String? = null,
)

package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodeUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = false)
class KlassApiCodeValidatorTest(private val validator: Validator) {
    @Test
    fun `invalid klass codes`() {
        val codes = listOf("23", "33")
        assertThat(KlassCodeUtil.isValidCode(codes)).isFalse()
    }

    @Test
    fun `valid klass codes`() {
        val codes = listOf("01", "02", "03")
        assertThat(KlassCodeUtil.isValidCode(codes)).isTrue()
    }

    @Test
    fun `valid and invalid klass codes`() {
        val codes = listOf("01", "33")
        assertThat(KlassCodeUtil.isValidCode(codes)).isFalse()
    }

    @Test
    fun `klass code validation legal codes`() {
        assertThat(validator.validate(TestCodeObject(listOf("01", "02", "03")))).isEmpty()
    }

    @Test
    fun `klass code validation illegal and legal code`() {
        assertThat(validator.validate(TestCodeObject(listOf("01", "33")))).isNotEmpty()
    }

    @Test
    fun `klass code validation illegal code`() {
        assertThat(validator.validate(TestCodeObject(listOf("999", "33")))).isNotEmpty()
    }
}

@Introspected
data class TestCodeObject(
    @KlassCode var codes: List<String>? = null,
)

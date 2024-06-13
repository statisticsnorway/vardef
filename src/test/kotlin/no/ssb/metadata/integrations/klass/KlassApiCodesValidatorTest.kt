package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodes
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodesUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest(startApplication = false)
class KlassApiCodesValidatorTest(private val validator: Validator) {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "19", "22", "7", "33", "8",
        ],
    )
    fun `invalid klass codes`(code: String) {
        val codes = listOf("23", "33")
        assertThat(KlassCodesUtil.isValidCodes(codes)).isFalse()
    }

    @Test
    fun `valid klass codes`() {
        val codes = listOf("01", "02", "03")
        assertThat(KlassCodesUtil.isValidCodes(codes)).isTrue()
    }

    @Test
    fun `valid and invalid klass codes`() {
        val codes = listOf("01", "33")
        assertThat(KlassCodesUtil.isValidCodes(codes)).isFalse()
    }

    @Test
    fun `klass code validation legal codes`() {
        assertThat(validator.validate(TestCodesObject(listOf("01", "02", "03")))).isEmpty()
    }

    @Test
    fun `klass code validation illegal and legal code`() {
        val result = validator.validate(TestCodesObject(listOf("01", "33")))
        assertThat(result).isNotEmpty()
        assertThat(result.map { res -> res.message }).isEqualTo(listOf("Invalid klass codes ([01, 33])"))
    }

    @Test
    fun `klass code validation illegal code`() {
        val result = validator.validate(TestCodesObject(listOf("999", "33")))
        assertThat(result.map { res -> res.message }).isEqualTo(listOf("Invalid klass codes ([999, 33])"))
        assertThat(result.map { res -> res.invalidValue }).isEqualTo(listOf(listOf("999", "33")))
    }
}

@Introspected
data class TestCodesObject(
    @KlassCodes var codes: List<String>? = null,
)

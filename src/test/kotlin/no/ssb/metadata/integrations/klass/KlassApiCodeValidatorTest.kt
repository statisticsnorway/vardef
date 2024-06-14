package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.ValidKlassCode.isValidKlassCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/*
TODO: Edit tests when caching object is implemented
 */
@MicronautTest(startApplication = false)
class KlassApiCodeValidatorTest(private val validator: Validator) {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "11",
            "232",
            "33",
        ],
    )
    fun `invalid klass code`(code: String) {
        assertFalse(isValidKlassCode(code,"702"))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "01",
            "02",
            "03",
        ],
    )
    fun `valid klass code`(code: String) {
        assertTrue(isValidKlassCode(code,"702"))
    }

    @Test
    fun `null klass code`() {
        assertFalse(isValidKlassCode(null,"702"))
    }

    @Test
    fun `empty klass code`() {
        assertFalse(isValidKlassCode("","702"))
    }

    @Test
    fun `klass code validation legal codes`() {
        assertThat(validator.validate(TestCodeObject(listOf("01", "02", "03"), listOf( "al",
            "al03",
            "al04")))).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "al",
            "al03",
            "al04",
        ],
    )
    fun `valid klass code subject field`(code: String) {
        assertTrue(isValidKlassCode(code,"618"))
    }

    @Test
    fun `klass code validation illegal and legal code`() {
        val result = validator.validate(TestCodeObject(listOf("01", "33"),null))
        assertThat(result).isNotEmpty()
        assertThat(result.map { res -> res.invalidValue }.contains("33")).isTrue()
        assertThat(result.map { res -> res.invalidValue }.contains("01")).isFalse()
        assertThat(result).hasSize(1)

        assertTrue(
            result
                .any {
                    it.invalidValue == "33" &&
                        it.message == "Invalid klass code (33)"
                },
        )
    }

    @Test
    fun `klass code validation empty code`() {
        val result = validator.validate(TestCodeObject(listOf("")))
        assertThat(result).isNotEmpty()
        assertThat(result.map { res -> res.invalidValue }.contains("")).isTrue()
        assertThat(result).hasSize(1)

        assertTrue(
            result
                .any {
                    it.invalidValue == "" &&
                        it.message == "Invalid klass code ()"
                },
        )
    }

    @Test
    fun `klass code validation illegal code`() {
        val result = validator.validate(TestCodeObject(listOf("999", "33")))
        assertThat(
            result.map {
                    res ->
                res.message
            },
        ).isEqualTo(listOf("Invalid klass code (999)", "Invalid klass code (33)"))
        assertThat(result.elementAt(0).message).isEqualTo("Invalid klass code (999)")
        assertThat(result.elementAt(1).message).isEqualTo("Invalid klass code (33)")
        assertThat(result.elementAt(1).invalidValue).isEqualTo("33")
        assertThat(result).isNotEmpty()
        assertThat(result).hasSize(2)
    }
}

/*
TODO: Remove/replace when caching object is implemented
 */
@Introspected
data class TestCodeObject(
    var unitCodes: List<@KlassCode("702") String>? = null,
    var subjectCodes: List<@KlassCode("618") String>? = null,
)

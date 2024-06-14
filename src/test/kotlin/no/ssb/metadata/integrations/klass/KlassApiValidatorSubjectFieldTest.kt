package no.ssb.metadata.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.KlassCodeSubjectField
import no.ssb.metadata.vardef.integrations.klass.validators.klasscode.ValidKlassCodeSubjectField.isValidSubjectCode
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
class KlassApiSubjectFieldValidatorTest(private val validator: Validator) {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "11",
            "232",
            "33",
        ],
    )
    fun `invalid klass code`(code: String) {
        assertFalse(isValidSubjectCode(code))
    }

    @Test
    fun `empty klass code`() {
        assertFalse(isValidSubjectCode(""))
    }

    @Test
    fun `null klass code`() {
        assertFalse(isValidSubjectCode(null))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "al",
            "al03",
            "al04",
        ],
    )
    fun `valid klass code`(code: String) {
        assertTrue(isValidSubjectCode(code))
    }

    @Test
    fun `legal and illegal klass codes`() {
        val result = validator.validate(TestCodeSubjectFieldObject(listOf("01", "al")))
        assertThat(result).isNotEmpty()
        assertThat(result.map { res -> res.invalidValue }.contains("01")).isTrue()
        assertThat(result.map { res -> res.invalidValue }.contains("al")).isFalse()
        assertThat(result).hasSize(1)

        assertTrue(
            result
                .any {
                    it.invalidValue == "01" &&
                        it.message == "Invalid klass code for subject field (01)"
                },
        )
    }

    @Test
    fun `klass code validation empty code`() {
        val result = validator.validate(TestCodeSubjectFieldObject(listOf("")))
        assertThat(result).isNotEmpty()
        assertThat(result.map { res -> res.invalidValue }.contains("")).isTrue()
        assertThat(result).hasSize(1)

        assertTrue(
            result
                .any {
                    it.invalidValue == "" &&
                        it.message == "Invalid klass code for subject field ()"
                },
        )
    }
}

/*
TODO: Remove/replace when caching object is implemented
 */
@Introspected
data class TestCodeSubjectFieldObject(
    var subjectFieldCodes: List<@KlassCodeSubjectField String>? = null,
)

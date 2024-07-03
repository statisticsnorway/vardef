package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Introspected
data class TestCodeObject(
    var unitCodes: List<
            @KlassCode("702")
            String,
            >? = null,
    var subjectCodes: List<
            @KlassCode("618")
            String,
            >? = null,
)

@MicronautTest(startApplication = true)
class KlassApiCodeValidatorTest(
    private val validator: Validator,
) {
    @Test
    fun `klass code validation legal codes`() {
        assertThat(
            validator.validate(
                TestCodeObject(
                    listOf("01", "02", "03"),
                    listOf(
                        "al",
                        "al03",
                        "al04",
                    ),
                ),
            ),
        ).isEmpty()
    }

    @Test
    fun `klass code validation illegal and legal code`() {
        val result = validator.validate(TestCodeObject(listOf("01", "33"), null))
        assertThat(result).isNotEmpty()
        assertThat(result.map { res -> res.invalidValue }.contains("33")).isTrue()
        assertThat(result.map { res -> res.invalidValue }.contains("01")).isFalse()
        assertThat(result).hasSize(1)

        assertTrue(
            result
                .any {
                    it.invalidValue == "33" &&
                        it.message == "Code 33 is not a member of classification with id 702"
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
                        it.message == "Code  is not a member of classification with id 702"
                },
        )
    }

    @Test
    fun `klass code validation illegal code`() {
        val result = validator.validate(TestCodeObject(listOf("999", "33")))
        assertThat(
            result.map { res ->
                res.message
            },
        ).isEqualTo(
            listOf(
                "Code 999 is not a member of classification with id 702",
                "Code 33 is not a member of classification with id 702",
            ),
        )
        assertThat(result.elementAt(1).invalidValue).isEqualTo("33")
        assertThat(result).isNotEmpty()
        assertThat(result).hasSize(2)
    }
}

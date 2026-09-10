package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.core.annotation.Introspected
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import no.ssb.metadata.vardef.annotations.KlassCode
import no.ssb.metadata.vardef.annotations.KlassCodeAtLevel
import no.ssb.metadata.vardef.annotations.KlassId
import no.ssb.metadata.vardef.constants.KLASS_ID_MEASUREMENT_TYPE
import no.ssb.metadata.vardef.constants.KLASS_ID_SUBJECT_FIELDS
import no.ssb.metadata.vardef.constants.KLASS_ID_UNIT_TYPES
import no.ssb.metadata.vardef.constants.KLASS_MEASUREMENT_TYPE_LEVEL
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Introspected
data class TestCodeObject(
    var unitCodes: List<
            @KlassCode(KLASS_ID_UNIT_TYPES)
            String,
            >? = null,
    var subjectCodes: List<
            @KlassCode(KLASS_ID_SUBJECT_FIELDS)
            String,
            >? = null,
    var measurementType: List<
            @KlassCodeAtLevel(KLASS_ID_MEASUREMENT_TYPE, KLASS_MEASUREMENT_TYPE_LEVEL)
            String,
            >? = null,
)

@Introspected
data class TestIdObject(
    var id: @KlassId String,
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
                    listOf("01", "08", "28"),
                    listOf(
                        "al",
                        "bb",
                        "vf",
                    ),
                    listOf(
                        "05",
                        "17",
                        "06",
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
                        it.message == "Code 33 is not a member of classification with id $KLASS_ID_UNIT_TYPES"
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
                        it.message == "Code  is not a member of classification with id $KLASS_ID_UNIT_TYPES"
                },
        )
    }

    @Test
    fun `klass id validation illegal code`() {
        val result = validator.validate(TestCodeObject(listOf("999", "33")))
        assertThat(
            result.map { res ->
                res.message
            },
        ).isEqualTo(
            listOf(
                "Code 999 is not a member of classification with id $KLASS_ID_UNIT_TYPES",
                "Code 33 is not a member of classification with id $KLASS_ID_UNIT_TYPES",
            ),
        )
        assertThat(result.elementAt(1).invalidValue).isEqualTo("33")
        assertThat(result).isNotEmpty()
        assertThat(result).hasSize(2)
    }

    @Test
    fun `klass code validation illegal code`() {
        val result = validator.validate(TestIdObject(("999")))
        assertThat(
            result.map { res ->
                res.message
            },
        ).isEqualTo(
            listOf(
                "Code 999 is not a valid classification id",
            ),
        )
    }

    @Test
    fun `klass id validation legal ids`() {
        assertThat(
            validator.validate(
                TestIdObject("91"),
            ),
        ).isEmpty()
    }
}

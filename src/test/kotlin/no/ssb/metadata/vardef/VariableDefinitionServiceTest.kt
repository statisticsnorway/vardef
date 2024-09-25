package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class VariableDefinitionServiceTest : BaseVardefTest() {
    @Test
    fun `get latest patch`() {
        assertThat(variableDefinitionService.getLatestPatchById(SAVED_VARIABLE_DEFINITION.definitionId).patchId)
            .isEqualTo(7)
    }

    @Test
    fun `get valid period at date`() {
        assertThat(
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    SAVED_VARIABLE_DEFINITION.definitionId,
                    LocalDate.of(1990, 1, 1),
                ).patchId,
        ).isEqualTo(6)
    }

    @Test
    fun `get valid period at date before range`() {
        assertThrows<NoMatchingValidityPeriodFound> {
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    SAVED_VARIABLE_DEFINITION.definitionId,
                    LocalDate.of(1760, 1, 1),
                )
        }
    }

    @Test
    fun `get valid period at date after range`() {
        assertThat(
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    SAVED_VARIABLE_DEFINITION.definitionId,
                    LocalDate.of(3000, 1, 1),
                ).patchId,
        ).isEqualTo(7)
    }

    @Test
    fun `list variable definition`() {
        variableDefinitionService
            .listAllAndRenderForLanguage(
                SupportedLanguages.EN,
                LocalDate.now(),
            ).let { renderedVariableDefinitions -> assertThat(renderedVariableDefinitions.size).isEqualTo(3) }
    }

    @ParameterizedTest
    @CsvSource(
        "1990,false",
        "1760,true",
        "3000,true",
    )
    fun `validate valid_from values`(
        year: Int,
        expected: Boolean,
    ) {
        assertThat(
            variableDefinitionService.isValidValidFromValue(
                SAVED_VARIABLE_DEFINITION.definitionId,
                LocalDate.of(year, 1, 1),
            ),
        ).isEqualTo(expected)
    }

    @Test
    fun `get id with only one patch`() {
        variableDefinitionService.save(SINGLE_SAVED_VARIABLE_DEFINITION)
        assertThat(
            variableDefinitionService.isValidValidFromValue(
                SINGLE_SAVED_VARIABLE_DEFINITION.definitionId,
                LocalDate.of(3000, 1, 1),
            ),
        ).isEqualTo(true)
    }

    companion object {
        @JvmStatic
        fun provideTestDataCheckDefinition(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        definition =
                            LanguageStringType(
                                nb = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
                                nn = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
                                en = "Income tax",
                            ),
                    ),
                    false,
                ),
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        definition =
                            LanguageStringType(
                                nb = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt. Liten endring",
                                nn = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt. Liten endring",
                                en = "Income tax. small change",
                            ),
                    ),
                    true,
                ),
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        definition =
                            LanguageStringType(
                                nb = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt. Liten endring",
                                nn = "Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
                                en = "Income tax",
                            ),
                    ),
                    false,
                ),
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                    ),
                    true,
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("provideTestDataCheckDefinition")
    fun `check definition texts for all languages`(
        inputObject: ValidityPeriod,
        expected: Boolean,
    ) {
        val actualResult = variableDefinitionService.isNewDefinition(inputObject, SAVED_VARIABLE_DEFINITION)
        assertThat(actualResult).isEqualTo(expected)
    }

    @ParameterizedTest
    @CsvSource(
        "intskatt, true",
        "nyttkortnavn, false",
    )
    fun `check if short name is valid`(
        shortName: String,
        expectedResult: Boolean,
    ) {
        assertThat(variableDefinitionService.checkIfShortNameExists(shortName)).isEqualTo(expectedResult)
    }
}

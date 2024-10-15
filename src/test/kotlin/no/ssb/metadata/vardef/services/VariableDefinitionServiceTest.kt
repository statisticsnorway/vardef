package no.ssb.metadata.vardef.services

import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.types.ObjectId
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
        assertThat(patches.getLatestPatchById(INCOME_TAX_VP1_P1.definitionId).patchId)
            .isEqualTo(numIncomeTaxPatches)
    }

    @Test
    fun `get valid period at date`() {
        assertThat(
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    INCOME_TAX_VP1_P1.definitionId,
                    LocalDate.of(1990, 1, 1),
                ).patchId,
        ).isEqualTo(7)
    }

    @Test
    fun `get valid period at date before range`() {
        assertThrows<NoMatchingValidityPeriodFound> {
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    INCOME_TAX_VP1_P1.definitionId,
                    LocalDate.of(1760, 1, 1),
                )
        }
    }

    @Test
    fun `get valid period at date after range`() {
        assertThat(
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    INCOME_TAX_VP1_P1.definitionId,
                    LocalDate.of(3000, 1, 1),
                ).patchId,
        ).isEqualTo(INCOME_TAX_VP2_P6.patchId)
    }

    @Test
    fun `list variable definition`() {
        variableDefinitionService
            .listAllAndRenderForLanguage(
                SupportedLanguages.EN,
                LocalDate.now(),
            ).let { renderedVariableDefinitions ->
                assertThat(renderedVariableDefinitions.size).isEqualTo(
                    NUM_ALL_VARIABLE_DEFINITIONS,
                )
            }
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
                INCOME_TAX_VP1_P1.definitionId,
                LocalDate.of(year, 1, 1),
            ),
        ).isEqualTo(expected)
    }

    @Test
    fun `get id with only one patch`() {
        val singleSavedTaxExample =
            INCOME_TAX_VP1_P1.copy(
                id = ObjectId(),
                definitionId = NanoId.generate(8),
            )
        variableDefinitionService.save(singleSavedTaxExample)
        assertThat(
            variableDefinitionService.isValidValidFromValue(
                singleSavedTaxExample.definitionId,
                LocalDate.of(3000, 1, 1),
            ),
        ).isEqualTo(true)
    }

    companion object {
        @JvmStatic
        fun provideTestDataCheckDefinition(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "No change",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                "Intektsskatt ny definisjon",
                                "Intektsskatt ny definisjon",
                                "Income tax new definition",
                            ),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "All languages appended",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Intektsskatt ny definisjon. Liten endring",
                                nn = "Intektsskatt ny definisjon. Liten endring",
                                en = "Income tax new definition. small change",
                            ),
                    ),
                    true,
                ),
                Arguments.argumentSet(
                    "One language appended",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Intektsskatt ny definisjon. Liten endring",
                                nn = "Intektsskatt ny definisjon",
                                en = "Income tax new definition",
                            ),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "All languages completely new text",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                    ),
                    true,
                ),
                Arguments.argumentSet(
                    "All languages null",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = null,
                                nn = null,
                                en = null,
                            ),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "One language null",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Intektsskatt ny definisjon",
                                nn = null,
                                en = "Income tex new definition",
                            ),
                    ),
                    false,
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("provideTestDataCheckDefinition")
    fun `check definition texts for all languages`(
        inputObject: ValidityPeriod,
        expected: Boolean,
    ) {
        val actualResult = variableDefinitionService.isNewDefinition(INCOME_TAX_VP1_P1.definitionId, inputObject)
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

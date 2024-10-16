package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.exceptions.DefinitionTextUnchangedException
import no.ssb.metadata.vardef.exceptions.InvalidValidFromException
import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SavedVariableDefinition
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
        assertThat(patches.latest(INCOME_TAX_VP1_P1.definitionId).patchId)
            .isEqualTo(numIncomeTaxPatches)
    }

    @Test
    fun `get valid period at date`() {
        assertThat(
            validityPeriods
                .getForDate(
                    INCOME_TAX_VP1_P1.definitionId,
                    LocalDate.of(1990, 1, 1),
                ).patchId,
        ).isEqualTo(7)
    }

    @Test
    fun `get valid period at date before range`() {
        assertThrows<NoMatchingValidityPeriodFound> {
            validityPeriods
                .getForDate(
                    INCOME_TAX_VP1_P1.definitionId,
                    LocalDate.of(1760, 1, 1),
                )
        }
    }

    @Test
    fun `get valid period at date after range`() {
        assertThat(
            validityPeriods
                .getForDate(
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
    @MethodSource("validFromTestCases")
    fun `validate valid_from values in new validity period`(
        inputObject: ValidityPeriod,
        expectSuccess: Boolean,
    ) {
        if (!expectSuccess) {
            assertThrows<InvalidValidFromException> {
                validityPeriods.create(
                    INCOME_TAX_VP1_P1.definitionId,
                    inputObject,
                )
            }
        } else {
            assertThat(
                validityPeriods.create(
                    INCOME_TAX_VP1_P1.definitionId,
                    inputObject,
                ),
            ).isInstanceOf(SavedVariableDefinition::class.java)
        }
    }

    @ParameterizedTest
    @MethodSource("definitionTextTestCases")
    fun `validate updated definition texts in new validity period`(
        inputObject: ValidityPeriod,
        expectSuccess: Boolean,
    ) {
        if (!expectSuccess) {
            assertThrows<DefinitionTextUnchangedException> {
                validityPeriods.create(
                    INCOME_TAX_VP1_P1.definitionId,
                    inputObject,
                )
            }
        } else {
            assertThat(
                validityPeriods.create(
                    INCOME_TAX_VP1_P1.definitionId,
                    inputObject,
                ),
            ).isInstanceOf(SavedVariableDefinition::class.java)
        }
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

    companion object {
        @JvmStatic
        fun validFromTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "Between existing periods",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(1990, 1, 1),
                    ),
                    false,
                ),
                Arguments.argumentSet(
                    "Before existing periods",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(1760, 1, 1),
                    ),
                    true,
                ),
                Arguments.argumentSet(
                    "After existing periods",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        definition =
                            LanguageStringType(
                                nb = "Endring",
                                nn = "Endring",
                                en = "Endring",
                            ),
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    true,
                ),
            )

        @JvmStatic
        fun definitionTextTestCases(): Stream<Arguments> =
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
                        validFrom = LocalDate.of(3000, 1, 1),
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
                        validFrom = LocalDate.of(3000, 1, 1),
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
                        validFrom = LocalDate.of(3000, 1, 1),
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
                        validFrom = LocalDate.of(3000, 1, 1),
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
                        validFrom = LocalDate.of(3000, 1, 1),
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
                        validFrom = LocalDate.of(3000, 1, 1),
                    ),
                    false,
                ),
            )
    }
}

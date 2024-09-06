package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.*
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.SINGLE_SAVED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class VariableDefinitionServiceTest : BaseVardefTest() {
    @BeforeEach
    fun setUpServiceTest() {
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.copy().apply { patchId = 2 })
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.copy().apply { patchId = 3 })
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 4
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 5
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 6
            },
        )

        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(2021, 1, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på siden",
                        nn = "For personer født på siden",
                        en = "Persons born on the side",
                    )
                patchId = 7
            },
        )
    }

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
                LocalDate.now(),
            )
            .let { renderedVariableDefinitions -> assertThat(renderedVariableDefinitions.size).isEqualTo(3) }
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
        fun provideTestDataCheckDefinition(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        definition =
                            LanguageStringType(
                                nb = "For personer født",
                                nn = "For personer født",
                                en = "Country background is",
                            ),
                    ),
                    false,
                ),
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        definition =
                            LanguageStringType(
                                nb = "For personer født i går",
                                nn = "For personer født i går",
                                en = "Persons born yesterday",
                            ),
                    ),
                    true,
                ),
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        definition =
                            LanguageStringType(
                                nb = "For personer født",
                                nn = "For personer født",
                                en = "Country background is",
                            ),
                    ),
                    false,
                ),
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        definition =
                            LanguageStringType(
                                nb = "For personer født i går",
                                nn = "For personer født",
                                en = "Country background is",
                            ),
                    ),
                    false,
                ),
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        definition =
                            LanguageStringType(
                                nb = "Hester og kuer født",
                                nn = "Hester og kuer født",
                                en = "Horses and cows born",
                            ),
                    ),
                    true,
                ),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestDataCheckDefinition")
    fun `check definition texts for all languages`(
        inputObject: InputVariableDefinition,
        expected: Boolean,
    ) {
        val actualResult = variableDefinitionService.isNewDefinition(inputObject, SAVED_VARIABLE_DEFINITION)
        assertThat(actualResult).isEqualTo(expected)
    }
}

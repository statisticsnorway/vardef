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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

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
                LocalDate.now(),
            )
            .let { renderedVariableDefinitions -> assertThat(renderedVariableDefinitions.size).isEqualTo(1) }
        // .let { renderedVariableDefinitions -> assertThat(renderedVariableDefinitions.size).isEqualTo(3) }
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#provideTestDataCheckDefinition")
    fun `check definition texts for all languages`(
        inputObject: InputVariableDefinition,
        expected: Boolean,
    ) {
        val actualResult = variableDefinitionService.isNewDefinition(inputObject, SAVED_VARIABLE_DEFINITION)
        assertThat(actualResult).isEqualTo(expected)
    }

    @Test
    fun `check definition changed for two languages present`() {
        val savedVariableDefinitionTwoLanguages =
            SAVED_VARIABLE_DEFINITION.copy(
                definition =
                    LanguageStringType(
                        nb = "For personer født",
                        nn = null,
                        en = "Country background is",
                    ),
            )
        val result =
            variableDefinitionService.isNewDefinition(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "For personer født",
                            nn = null,
                            en = "Country background is",
                        ),
                ),
                savedVariableDefinitionTwoLanguages,
            )
        assertThat(result).isFalse()
        val result2 =
            variableDefinitionService.isNewDefinition(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "For personer født i går",
                            nn = null,
                            en = "Country background is born",
                        ),
                ),
                savedVariableDefinitionTwoLanguages,
            )
        assertThat(result2).isTrue()
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

    @Test
    fun `get all validity periods`() {
        val result = variableDefinitionService.listAllPatchesById(SAVED_VARIABLE_DEFINITION.definitionId)
        assertThat(result.size).isEqualTo(7)
        assertThat(result[6].patchId).isEqualTo(7)
        val groupedByDate = result.groupBy { it.validFrom }
        assertThat(groupedByDate.size).isEqualTo(3)
        val firstDate = result.minOfOrNull { it.validFrom }
        val lastDate = result.maxOfOrNull { it.validFrom } // Find the last (most recent) date

        val filteredLastDate = result.filter { it.validFrom == lastDate }
        val filterFirstDate = result.filter { it.validFrom == firstDate }
        assertThat(filteredLastDate.size).isEqualTo(1)
        assertThat(filterFirstDate.size).isEqualTo(3)
        // filteredObjects.forEach { println(it) }
    }
}

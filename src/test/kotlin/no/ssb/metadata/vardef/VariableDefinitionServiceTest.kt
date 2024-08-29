package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.SINGLE_SAVED_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.utils.*
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
            .isEqualTo(6)
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
        ).isEqualTo(6)
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
    @MethodSource("TestUtils#provideTestDataCheckDefinition")
    fun `check definition texts for all languages`(
        inputObject: InputVariableDefinition,
        expected: Boolean,
    ) {
        val actualResult = variableDefinitionService.isNewDefinition(inputObject, SAVED_VARIABLE_DEFINITION)
        assertThat(actualResult).isEqualTo(expected)
    }

    @Test
    fun `check definition changed for two languages present`() {
        val result =
            variableDefinitionService.isNewDefinition(
                INPUT_VARIABLE_DEFINITIONS_UNCHANGED_TWO_LANGUAGES,
                SAVED_VARIABLE_DEFINITION_TWO_LANGUAGES,
            )
        assertThat(result).isFalse()
        val result2 =
            variableDefinitionService.isNewDefinition(
                INPUT_VARIABLE_DEFINITIONS_CHANGED_TWO_LANGUAGES,
                SAVED_VARIABLE_DEFINITION_TWO_LANGUAGES,
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
}

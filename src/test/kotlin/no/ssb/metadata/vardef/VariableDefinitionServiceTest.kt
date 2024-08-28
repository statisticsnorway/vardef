package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate


class VariableDefinitionServiceTest : BaseVardefTest() {
    @Test
    fun `get latest patch`() {
        assertThat(variableDefinitionService.getLatestPatchById(SAVED_VARIABLE_DEFINITION.definitionId).patchId).isEqualTo(6)
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
        assertThrows<NoMatchingValidityPeriodFound> {
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    SAVED_VARIABLE_DEFINITION.definitionId,
                    LocalDate.of(3000, 1, 1),
                )
        }
    }


    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestDataProvider#provideTestDataCheckDefinition")
    fun `check definition texts for all languages`(inputObject: InputVariableDefinition, expected: Boolean) {
        val actualResult = variableDefinitionService.isNewDefinition(inputObject, SAVED_VARIABLE_DEFINITION)
        assertThat(actualResult).isEqualTo(expected)
    }

    @Test
    fun `check definition changed for two languages present`(){
        val result = variableDefinitionService.isNewDefinition(
            INPUT_VARIABLE_DEFINITIONS_UNCHANGED_TWO_LANGUAGES,
            SAVED_VARIABLE_DEFINITION_TWO_LANGUAGES
        )
        assertThat(result).isFalse()
        val result2 = variableDefinitionService.isNewDefinition(
            INPUT_VARIABLE_DEFINITIONS_CHANGED_TWO_LANGUAGES,
            SAVED_VARIABLE_DEFINITION_TWO_LANGUAGES
        )
        assertThat(result2).isTrue()
    }

}

package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    @Test
    fun `add new language and definition text for languages not changed`() {
        val result = variableDefinitionService.isNewDefinition(INPUT_VARIABLE_DEFINITION, SAVED_VARIABLE_DEFINITION)
        assertThat(result).isFalse()
    }

    @Test
    fun `definition text changed for all languages present`(){
        val result =
            variableDefinitionService.isNewDefinition(
                INPUT_VARIABLE_DEFINITION_NEW_DEFINITION,
                SAVED_VARIABLE_DEFINITION,
            )
        assertThat(result).isTrue()
    }

    @Test
    fun `definition text not changed`(){
        val result = variableDefinitionService.isNewDefinition(
            INPUT_VARIABLE_DEFINITION_NO_NEW_DEFINITIONS,
            SAVED_VARIABLE_DEFINITION
        )
        assertThat(result).isFalse()
    }
}

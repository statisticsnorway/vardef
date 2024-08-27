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
    fun `save new validity period definition is changed`() {
        val result = variableDefinitionService.saveNewValidityPeriod(INPUT_VARIABLE_DEFINITION_NEW_DEFINITION.toSavedVariableDefinition(3))
        assertThat(result).isNotNull
        assertThat(result.definitionId).isEqualTo(INPUT_VARIABLE_DEFINITION.id)
        assertThat(result.definition).isNotEqualTo(INPUT_VARIABLE_DEFINITION.definition)
    }

    @Test
    fun `check definition text is changed`() {
        val latestExistingPatch = variableDefinitionService.getLatestPatchById(SAVED_VARIABLE_DEFINITION.definitionId)
        val result = variableDefinitionService.checkDefinition(INPUT_VARIABLE_DEFINITION, latestExistingPatch.definitionId)
        assertThat(result).isTrue()
    }

    @Test
    fun `check definition text all languages`() {
        val result = variableDefinitionService.checkAllLanguages(INPUT_VARIABLE_DEFINITION, SAVED_VARIABLE_DEFINITION.definitionId)
        assertThat(result).isFalse()
        val result2 =
            variableDefinitionService.checkAllLanguages(
                INPUT_VARIABLE_DEFINITION_NEW_DEFINITION,
                SAVED_VARIABLE_DEFINITION.definitionId,
            )
        assertThat(result2).isTrue()
    }
}

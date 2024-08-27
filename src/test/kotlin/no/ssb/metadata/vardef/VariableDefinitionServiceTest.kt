package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.exceptions.NoMatchingValidityPeriodFound
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
        assertThrows<NoMatchingValidityPeriodFound> {
            variableDefinitionService
                .getLatestPatchByDateAndById(
                    SAVED_VARIABLE_DEFINITION.definitionId,
                    LocalDate.of(3000, 1, 1),
                )
        }
    }

    @Test
    fun `list variable definition`() {
        variableDefinitionService
            .listAllAndRenderForLanguage(
                SupportedLanguages.EN,
                LocalDate.now(),
                LocalDate.now(),
            )
            .onEach { println(it) }
            .let { renderedVariableDefinitions -> assertThat(renderedVariableDefinitions.size).isEqualTo(2) }
    }
}

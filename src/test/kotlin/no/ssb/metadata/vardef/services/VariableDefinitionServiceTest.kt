package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.NUM_ALL_VARIABLE_DEFINITIONS
import no.ssb.metadata.vardef.utils.numIncomeTaxPatches
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate

class VariableDefinitionServiceTest : BaseVardefTest() {
    @Test
    fun `get latest patch`() {
        assertThat(patches.latest(INCOME_TAX_VP1_P1.definitionId).patchId)
            .isEqualTo(numIncomeTaxPatches)
    }

    @Test
    fun `list variable definition`() {
        variableDefinitionService
            .listForDateAndRender(
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
        "intskatt, true",
        "nyttkortnavn, false",
    )
    fun `check if short name is valid`(
        shortName: String,
        expectedResult: Boolean,
    ) {
        assertThat(variableDefinitionService.doesShortNameExist(shortName)).isEqualTo(expectedResult)
    }
}

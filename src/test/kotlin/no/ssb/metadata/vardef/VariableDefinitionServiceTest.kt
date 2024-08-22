package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class VariableDefinitionServiceTest : BaseVardefTest() {
    @Test
    fun `get latest version`() {
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION)
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.apply { versionId = 2 })
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.apply { versionId = 3 })

        assertThat(variableDefinitionService.getLatestVersionById(SAVED_VARIABLE_DEFINITION.definitionId).versionId).isEqualTo(3)
    }

    @Test
    fun `get valid period at date`() {
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.apply { versionId = 2 })
        variableDefinitionService.save(SAVED_VARIABLE_DEFINITION.apply { versionId = 3 })
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.apply {
                validFrom = LocalDate.of(1980, 12, 1)
                versionId = 4
            },
        )
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.apply {
                validFrom = LocalDate.of(1980, 12, 1)
                versionId = 5
            },
        )

        assertThat(
            variableDefinitionService
                .getLatestVersionByDateAndById(
                    SAVED_VARIABLE_DEFINITION.definitionId,
                    LocalDate.of(1990, 1, 1),
                ).versionId,
        ).isEqualTo(5)
    }
}

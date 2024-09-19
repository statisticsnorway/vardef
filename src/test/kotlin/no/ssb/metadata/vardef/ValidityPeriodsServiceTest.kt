package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.models.InputValidityPeriod
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.Period
import java.util.stream.Stream


class ValidityPeriodsServiceTest : BaseVardefTest() {
    private val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId

    @Test
    fun `end validity period`() {
        val newValidityPeriodValidFrom = LocalDate.of(2024, 9, 2)

        val patchEndValidityPeriod =
            variableDefinitionService.endLastValidityPeriod(
                savedVariableDefinitionId,
                newValidityPeriodValidFrom,
            )
        val expectedNewPatchId =
            variableDefinitionService.getLatestPatchById(
                savedVariableDefinitionId,
            ).patchId
        val expectedValidUntil = newValidityPeriodValidFrom.minus(Period.ofDays(1))

        assertThat(patchEndValidityPeriod.validUntil).isAfter(patchEndValidityPeriod.validFrom)
        assertThat(patchEndValidityPeriod.patchId).isEqualTo(expectedNewPatchId)
        assertThat(patchEndValidityPeriod.validUntil).isEqualTo(expectedValidUntil)
    }

    companion object {
        @JvmStatic
        fun provideNewValidityPeriods(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        validFrom = LocalDate.now(),
                        validUntil = null,
                    ),
                ),
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        validFrom = LocalDate.of(2025, 10, 5),
                        validUntil = null,
                    ),
                ),
                Arguments.of(
                    INPUT_VALIDITY_PERIOD.copy(
                        validFrom = LocalDate.of(2050, 1, 1),
                        validUntil = null,
                    ),
                ),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideNewValidityPeriods")
    fun `save new validity period`(inputData: InputValidityPeriod) {
        val patches = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                inputData,
                savedVariableDefinitionId,
            )
        val patchesAfterSave =
            variableDefinitionService.listAllPatchesById(
                savedVariableDefinitionId,
            )

        val endValidityPeriodPatch =
            variableDefinitionService.getOnePatchById(
                savedVariableDefinitionId,
                saveNewValidityPeriod.patchId - 1,
            )

        assertThat(patchesAfterSave.size).isEqualTo(patches.size + 2)
        assertThat(saveNewValidityPeriod.patchId).isEqualTo(patches.last().patchId + 2)
        assertThat(saveNewValidityPeriod.patchId).isEqualTo(patchesAfterSave.last().patchId)
        assertThat(saveNewValidityPeriod.validFrom).isEqualTo(inputData.validFrom)
        assertThat(endValidityPeriodPatch.validUntil).isEqualTo(
            saveNewValidityPeriod.validFrom.minusDays(1),
        )
    }

    /*@Test
    fun `save new validity period before all valid from`() {
        val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId
        val patches = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                INPUT_VARIABLE_DEFINITION.copy(
                    id = savedVariableDefinitionId,
                    validFrom = LocalDate.of(1796, 1, 1),
                    validUntil = null,
                ),
                SAVED_VARIABLE_DEFINITION.definitionId,
            )
        val patchesAfterSave = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)

        assertThat(saveNewValidityPeriod.patchId).isEqualTo(4)
        assertThat(patchesAfterSave.size).isEqualTo(patches.size + 1)
        assertThat(saveNewValidityPeriod.validUntil).isEqualTo(
            patchesAfterSave.first().validFrom.minusDays(1),
        )
        assertThat(saveNewValidityPeriod.validFrom).isBefore(patchesAfterSave.first().validFrom)
    }*/
}
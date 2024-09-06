package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INPUT_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import java.time.LocalDate
import java.time.Period
import java.util.stream.Stream

class ValidityPeriodsTest : BaseVardefTest() {
    @BeforeEach
    fun setUpValidityPeriod() {
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1960, 1, 1)
                validUntil = LocalDate.of(1980, 11, 30)
                patchId = 2
            },
        )

        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født oppe",
                        nn = "For personer født oppe",
                        en = "Persons born upstairs",
                    )
                patchId = 3
            },
        )
    }

    @Test
    @DisplayName("Create new patch with valid until day before new validity period")
    fun `end validity period`() {
        val newValidityPeriodValidFrom = LocalDate.of(2024, 9, 2)
        val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId

        val newPatchEndValidityPeriod =
            variableDefinitionService.endLastValidityPeriod(
                savedVariableDefinitionId,
                newValidityPeriodValidFrom,
            )
        val expectedNewPatchId =
            variableDefinitionService.getLatestPatchById(
                savedVariableDefinitionId,
            ).patchId
        val expectedValidUntil = newValidityPeriodValidFrom.minus(Period.ofDays(1))

        assertThat(newPatchEndValidityPeriod.validUntil).isAfter(newPatchEndValidityPeriod.validFrom)
        assertThat(newPatchEndValidityPeriod.patchId).isEqualTo(expectedNewPatchId)
        assertThat(newPatchEndValidityPeriod.validUntil).isEqualTo(expectedValidUntil)
    }

    // create parameterized tests for these
    companion object {
        @JvmStatic
        fun saveValidityPeriod(): Stream<Arguments> {
            val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId
            return Stream.of(
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        id = savedVariableDefinitionId,
                        validFrom = LocalDate.now(),
                        validUntil = null,
                    ),
                    2,
                ),
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        id = savedVariableDefinitionId,
                        validFrom = LocalDate.of(1796, 1, 1),
                        validUntil = null,
                    ),
                    1,
                ),
                Arguments.of(
                    INPUT_VARIABLE_DEFINITION.copy(
                        id = savedVariableDefinitionId,
                        validFrom = LocalDate.of(2050, 1, 1),
                        validUntil = null,
                    ),
                    2,
                ),
            )
        }
    }

    @Test
    @DisplayName("Save a new validity period creates two new patches")
    fun `save new validity period valid from today`() {
        val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId
        val patches = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)

        val newValidityPeriod =
            INPUT_VARIABLE_DEFINITION.copy(
                id = savedVariableDefinitionId,
                validFrom = LocalDate.now(),
                validUntil = null,
            )

        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriod,
                savedVariableDefinitionId,
            )
        val patchesAfterSave =
            variableDefinitionService.listAllPatchesById(
                savedVariableDefinitionId,
            )
        val endValidityPeriod =
            variableDefinitionService.getOnePatchById(
                savedVariableDefinitionId,
                saveNewValidityPeriod.patchId - 1,
            )
        val startValidityPeriodPatchId = patchesAfterSave.last().patchId

        assertThat(patchesAfterSave.size).isEqualTo(patches.size + 2)

        assertThat(saveNewValidityPeriod.patchId).isEqualTo(startValidityPeriodPatchId)

        assertThat(saveNewValidityPeriod.validUntil).isNull()
        assertThat(saveNewValidityPeriod.validFrom).isEqualTo(newValidityPeriod.validFrom)
        assertThat(endValidityPeriod.validUntil).isEqualTo(
            newValidityPeriod.validFrom.minusDays(1),
        )
    }

    @Test
    @DisplayName("Save a new validity period before all valid from creates one patch with valid from and valid until")
    fun `save new validity period before all valid from`() {
        val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId
        val patches = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        val newValidityPeriodPreFirstPeriod =
            INPUT_VARIABLE_DEFINITION.copy(
                id = savedVariableDefinitionId,
                validFrom = LocalDate.of(1796, 1, 1),
                validUntil = null,
            )

        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriodPreFirstPeriod,
                savedVariableDefinitionId,
            )

        val patchesAfterSave = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)

        assertThat(saveNewValidityPeriod.patchId).isEqualTo(4)
        assertThat(saveNewValidityPeriod.validUntil).isEqualTo(
            patchesAfterSave.first().validFrom.minusDays(1),
        )
        assertThat(saveNewValidityPeriod.validFrom).isBefore(patchesAfterSave.first().validFrom)
    }

    @Test
    fun `save new validity period in the future`() {
        val savedVariableDefinitionId = SAVED_VARIABLE_DEFINITION.definitionId
        val patches = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        val newValidityPeriodFuture =
            INPUT_VARIABLE_DEFINITION.copy(
                id = savedVariableDefinitionId,
                validFrom = LocalDate.of(2050, 1, 1),
                validUntil = null,
            )
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                newValidityPeriodFuture,
                savedVariableDefinitionId,
            )
        val patchesAfterSave = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        assertThat(patchesAfterSave.size).isEqualTo(patches.size + 2)
        assertThat(saveNewValidityPeriod).isNotNull
        assertThat(saveNewValidityPeriod.patchId).isEqualTo(5)
        assertThat(
            variableDefinitionService.getOnePatchById(
                savedVariableDefinitionId,
                4,
            ).validUntil,
        ).isEqualTo(
            saveNewValidityPeriod.validFrom.minus(Period.ofDays(1)),
        )
        assertThat(patchesAfterSave[patchesAfterSave.size - 2].validUntil).isEqualTo(
            saveNewValidityPeriod.validFrom.minus(Period.ofDays(1)),
        )
        assertThat(patchesAfterSave[patchesAfterSave.size - 2].validUntil).isEqualTo(
            saveNewValidityPeriod.validFrom.minus(Period.ofDays(1)),
        )
    }
}

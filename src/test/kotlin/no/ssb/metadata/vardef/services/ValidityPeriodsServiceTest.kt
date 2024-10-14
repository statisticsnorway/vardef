package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.ValidityPeriod
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.VALIDITY_PERIOD_TAX_EXAMPLE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class ValidityPeriodsServiceTest : BaseVardefTest() {
    private val savedVariableDefinitionId = INCOME_TAX_VP1_P1.definitionId

    @Test
    fun `end validity period`() {
        val newValidityPeriodValidFrom = LocalDate.of(2024, 9, 2)
        val latestPatch = variableDefinitionService.getLatestPatchById(savedVariableDefinitionId)
        val patchEndValidityPeriod =
            variableDefinitionService.endLastValidityPeriod(
                savedVariableDefinitionId,
                newValidityPeriodValidFrom,
            )

        assertThat(patchEndValidityPeriod.validUntil).isAfter(patchEndValidityPeriod.validFrom)
        assertThat(patchEndValidityPeriod.patchId).isEqualTo(latestPatch.patchId + 1)
        assertThat(patchEndValidityPeriod.validUntil).isEqualTo(newValidityPeriodValidFrom.minusDays(1))
    }

    companion object {
        @JvmStatic
        fun provideNewValidityPeriods(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "Today's date",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        validFrom = LocalDate.now(),
                        definition =
                            LanguageStringType(
                                nb = "Ny def",
                                nn = "Ny def",
                                en = "New def",
                            ),
                    ),
                ),
                Arguments.argumentSet(
                    "2025-10-05",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        validFrom = LocalDate.of(2025, 10, 5),
                        definition =
                            LanguageStringType(
                                nb = "Ny def",
                                nn = "Ny def",
                                en = "New def",
                            ),
                    ),
                ),
                Arguments.argumentSet(
                    "2025-01-01",
                    VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                        validFrom = LocalDate.of(2050, 1, 1),
                        definition =
                            LanguageStringType(
                                nb = "Ny def",
                                nn = "Ny def",
                                en = "New def",
                            ),
                    ),
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("provideNewValidityPeriods")
    fun `save new validity period`(inputData: ValidityPeriod) {
        val patchesBefore = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        val newValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                inputData,
                savedVariableDefinitionId,
            )
        val patchesAfter =
            variableDefinitionService.listAllPatchesById(
                savedVariableDefinitionId,
            )

        val lastPatchInSecondToLastValidityPeriod =
            variableDefinitionService
                .listAllPatchesGroupedByValidityPeriods(savedVariableDefinitionId)
                .let { it.values.elementAt(it.values.size - 2) }
                ?.last()

        assertThat(patchesAfter.size).isEqualTo(patchesBefore.size + 2)
        assertThat(newValidityPeriod.patchId).isEqualTo(patchesBefore.last().patchId + 2)
        assertThat(newValidityPeriod.patchId).isEqualTo(patchesAfter.last().patchId)
        assertThat(newValidityPeriod.validFrom).isEqualTo(inputData.validFrom)
        assertThat(lastPatchInSecondToLastValidityPeriod?.validUntil).isEqualTo(
            newValidityPeriod.validFrom.minusDays(1),
        )
    }

    @Test
    fun `save new validity period before all valid from`() {
        val patches = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)
        val saveNewValidityPeriod =
            variableDefinitionService.saveNewValidityPeriod(
                VALIDITY_PERIOD_TAX_EXAMPLE.copy(
                    validFrom = LocalDate.of(1796, 1, 1),
                    definition =
                        LanguageStringType(
                            nb = "Ny def",
                            nn = "Ny def",
                            en = "New def",
                        ),
                ),
                INCOME_TAX_VP1_P1.definitionId,
            )
        val patchesAfterSave = variableDefinitionService.listAllPatchesById(savedVariableDefinitionId)

        assertThat(saveNewValidityPeriod.patchId).isEqualTo(patches.last().patchId + 1)
        assertThat(saveNewValidityPeriod.validUntil).isEqualTo(
            patchesAfterSave.first().validFrom.minusDays(1),
        )
        assertThat(saveNewValidityPeriod.validFrom).isBefore(patchesAfterSave.first().validFrom)
    }
}

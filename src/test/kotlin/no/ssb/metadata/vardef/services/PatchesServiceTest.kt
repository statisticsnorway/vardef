package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.Owner
import no.ssb.metadata.vardef.models.Patch
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class PatchesServiceTest : BaseVardefTest() {
    @Test
    fun `get latest patch`() {
        assertThat(patches.latest(INCOME_TAX_VP1_P1.definitionId).patchId)
            .isEqualTo(numIncomeTaxPatches)
    }

    @Test
    fun `list patches`() {
        assertThat(patches.list(INCOME_TAX_VP1_P1.definitionId).map { it.patchId }).isEqualTo((1..numIncomeTaxPatches).toList())
    }

    @Test
    fun `list patches unknown id`() {
        assertThrows<EmptyResultException> { patches.list("unknown id") }
    }

    @ParameterizedTest
    @MethodSource("getSpecificPatchTestCases")
    fun `get specific patch`(
        definitionId: String,
        patchId: Int,
        expectSuccess: Boolean,
    ) {
        if (expectSuccess) {
            assertThat(patches.get(definitionId, patchId).patchId).isEqualTo(patchId)
        } else {
            assertThrows<EmptyResultException> { patches.get(definitionId, patchId) }
        }
    }

    @Test
    fun `delete patches`() {
        patches.deleteAllForDefinitionId(INCOME_TAX_VP1_P1.definitionId)
        assertThat(
            variableDefinitionService.list().map { it.definitionId },
        ).doesNotContain(INCOME_TAX_VP1_P1.definitionId)
    }

    @Test
    fun `create patch owner field updated`(){

    }

    @Test
    fun `create patch owner field not updated`(){

    }

    @Test
    fun `create patch owner field and other value updated`(){

    }

    @ParameterizedTest
    @MethodSource("createPatchTestCases")
    fun `create patch`(
        latestPatchOnValidityPeriod: SavedVariableDefinition,
        patch: Patch,
        numPatchesCreated: Int,
        latestPatchNotSelectedPeriod: SavedVariableDefinition,
        fieldName: String,
    ) {
        // Create patch
        patches.createPatch(patch, INCOME_TAX_VP1_P1.definitionId, latestPatchOnValidityPeriod)

        // Retrieve all validity periods for the given definition ID
        val validityPeriodList = validityPeriods.list(INCOME_TAX_VP1_P1.definitionId)

        validityPeriodList.forEach { period ->
            // Retrieve property values based on the specified field name
            val periodValue = getPropertyByName(period, fieldName)
            val latestSelectedPatchValue = getPropertyByName(latestPatchOnValidityPeriod, fieldName)
            val latestPatchValue = getPropertyByName(latestPatchNotSelectedPeriod, fieldName)

            // Case: Owner field is updated - owner change across all periods
            if (fieldName.isBlank()) {
                assertThat(period.owner).isNotEqualTo(latestPatchOnValidityPeriod.owner)
                assertThat(period.owner).isNotEqualTo(latestPatchNotSelectedPeriod.owner)
            }

            // Case: Non-owner field is updated, only one patch created in selected period
            if (fieldName.isNotBlank() && numPatchesCreated == 1 && period.validFrom == latestPatchOnValidityPeriod.validFrom) {
                assertThat(periodValue).isNotEqualTo(latestSelectedPatchValue)
            }

            // Case: Both owner and other fields updated in the selected validity period
            if (fieldName.isNotBlank() && period.validFrom == latestPatchOnValidityPeriod.validFrom && numPatchesCreated > 1) {
                assertThat(periodValue).isNotEqualTo(latestSelectedPatchValue)
                assertThat(period.owner).isNotEqualTo(latestPatchOnValidityPeriod.owner)
            }

            // Case: Only owner is updated in non-selected validity periods
            if (fieldName.isNotBlank() && period.validFrom != latestPatchOnValidityPeriod.validFrom && numPatchesCreated > 1) {
                assertThat(periodValue).isEqualTo(latestPatchValue)
                assertThat(period.owner).isNotEqualTo(latestPatchNotSelectedPeriod.owner)
            }
        }
    }

    companion object {
        @JvmStatic
        fun createPatchTestCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.argumentSet(
                    "Update only owner selected validity period",
                    INCOME_TAX_VP1_P7,
                    INCOME_TAX_VP1_P7.copy(
                        owner =
                            Owner(
                                "dapla-felles",
                                listOf(
                                    "pers-skatt-developers",
                                    TEST_DEVELOPERS_GROUP,
                                    "neighbourhood-dogs",
                                    "dapla-felles-developers",
                                ),
                            ),
                    ).toPatch(),
                    2,
                    INCOME_TAX_VP2_P6,
                    "",
                ),
                Arguments.argumentSet(
                    "Update unit types and owner latest validity period",
                    INCOME_TAX_VP2_P6,
                    INCOME_TAX_VP2_P6.copy(
                        unitTypes = listOf("01", "02", "03"),
                        owner =
                            Owner(
                                "dapla-felles",
                                listOf(
                                    "pers-skatt-developers",
                                    TEST_DEVELOPERS_GROUP,
                                    "neighbourhood-dogs",
                                ),
                            ),
                    ).toPatch(),
                    2,
                    INCOME_TAX_VP1_P7,
                    "unitTypes",
                ),
                Arguments.argumentSet(
                    "Update name and owner selected period",
                    INCOME_TAX_VP1_P7,
                    INCOME_TAX_VP1_P7.copy(
                        name =
                            LanguageStringType(
                                nb = "navn",
                                nn = "namn",
                                en = "name",
                            ),
                        owner =
                            Owner(
                                "dapla-felles",
                                listOf(
                                    "pers-skatt-developers",
                                    TEST_DEVELOPERS_GROUP,
                                    "neighbourhood-dogs",
                                ),
                            ),
                    ).toPatch(),
                    2,
                    INCOME_TAX_VP2_P6,
                    "name",
                ),
                Arguments.argumentSet(
                    "Update not owner",
                    INCOME_TAX_VP2_P6,
                    INCOME_TAX_VP2_P6.copy(
                        name =
                            LanguageStringType(
                                nb = "navn",
                                nn = "namn",
                                en = "name",
                            ),
                    ).toPatch(),
                    1,
                    INCOME_TAX_VP1_P7,
                    "name",
                ),
            )
        }

        @JvmStatic
        fun getSpecificPatchTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.argumentSet(
                    "First patch draft",
                    DRAFT_BUS_EXAMPLE.definitionId,
                    1,
                    true,
                ),
                Arguments.argumentSet(
                    "Fifth patch published",
                    INCOME_TAX_VP1_P1.definitionId,
                    5,
                    true,
                ),
                Arguments.argumentSet(
                    "Unknown definition id",
                    "Unknown definition id",
                    1,
                    false,
                ),
                Arguments.argumentSet(
                    "Unknown patch id",
                    DRAFT_BUS_EXAMPLE.definitionId,
                    78452,
                    false,
                ),
                Arguments.argumentSet(
                    "Patch id 0",
                    DRAFT_BUS_EXAMPLE.definitionId,
                    0,
                    false,
                ),
            )
    }
}

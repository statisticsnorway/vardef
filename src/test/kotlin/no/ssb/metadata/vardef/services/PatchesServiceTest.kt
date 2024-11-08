package no.ssb.metadata.vardef.services

import io.micronaut.data.exceptions.EmptyResultException
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.Owner
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
    fun `create patch owner field updated across all periods`() {
        val patch =
            INCOME_TAX_VP1_P7
                .copy(
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
                ).toPatch()

        patches.create(patch, INCOME_TAX_VP1_P1.definitionId, INCOME_TAX_VP1_P7)
        val validityPeriodList = validityPeriods.listLatestByValidityPeriod(INCOME_TAX_VP1_P1.definitionId)
        validityPeriodList.forEach { period ->
            assertThat(period.owner).isNotEqualTo(INCOME_TAX_VP1_P7.owner)
            assertThat(period.owner).isNotEqualTo(INCOME_TAX_VP2_P6.owner)
        }
    }

    @Test
    fun `create patch owner field not updated`() {
        val patch =
            INCOME_TAX_VP2_P6
                .copy(
                    name =
                        LanguageStringType(
                            nb = "navn",
                            nn = "namn",
                            en = "name",
                        ),
                ).toPatch()

        patches.create(patch, INCOME_TAX_VP1_P1.definitionId, INCOME_TAX_VP2_P6)
        val validityPeriodList = validityPeriods.listLatestByValidityPeriod(INCOME_TAX_VP1_P1.definitionId)
        // Only one patch created in selected period
        validityPeriodList
            .filter { it.validFrom == INCOME_TAX_VP2_P6.validFrom }
            .forEach { period ->
                assertThat(period.name).isNotEqualTo(INCOME_TAX_VP2_P6.name)
            }
    }

    @Test
    fun `create patch owner and unit_types updated`() {
        val patch =
            INCOME_TAX_VP2_P6
                .copy(
                    unitTypes = listOf("01", "02", "03"),
                    owner =
                        Owner(
                            TEST_TEAM,
                            listOf(
                                "pers-skatt-developers",
                                TEST_DEVELOPERS_GROUP,
                                "neighbourhood-dogs",
                            ),
                        ),
                ).toPatch()

        patches.create(patch, INCOME_TAX_VP1_P1.definitionId, INCOME_TAX_VP2_P6)
        val validityPeriodList = validityPeriods.listLatestByValidityPeriod(INCOME_TAX_VP1_P1.definitionId)

        // For selected validity period
        validityPeriodList
            .filter { it.validFrom == INCOME_TAX_VP2_P6.validFrom }
            .forEach { period ->

                assertThat(period.unitTypes).isNotEqualTo(INCOME_TAX_VP2_P6.unitTypes)
                assertThat(period.owner).isNotEqualTo(INCOME_TAX_VP2_P6.owner)
            }
        // For all other validity periods
        validityPeriodList
            .filter { it.validFrom != INCOME_TAX_VP2_P6.validFrom }
            .forEach { period ->

                assertThat(period.unitTypes).isEqualTo(INCOME_TAX_VP1_P7.unitTypes)
                assertThat(period.owner).isNotEqualTo(INCOME_TAX_VP1_P7.owner)
            }
    }

    companion object {
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

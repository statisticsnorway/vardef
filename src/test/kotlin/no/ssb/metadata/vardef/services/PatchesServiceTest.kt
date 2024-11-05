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
import java.time.LocalDate
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
    fun `create patch update only owner`() {
        val latestPatchOnValidityPeriod =
            validityPeriods.getMatchingOrLatest(
                INCOME_TAX_VP1_P1.definitionId,
                validFrom = null,
            )
        val patch =
            INCOME_TAX_VP2_P6.copy(
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
        patches.createPatch(
            patch.toSavedVariableDefinition(
                latestPatchOnValidityPeriod.patchId,
                latestPatchOnValidityPeriod,
            ),
            latestPatchOnValidityPeriod,
        )
        val validityPeriods = validityPeriods.list(INCOME_TAX_VP1_P1.definitionId)
        assertThat(validityPeriods[0].patchId).isEqualTo(8)
        assertThat(validityPeriods[1].patchId).isEqualTo(9)
    }

    @Test
    fun `create patch update owner and unit types`() {
        val latestPatchOnValidityPeriod =
            validityPeriods.getMatchingOrLatest(
                INCOME_TAX_VP1_P1.definitionId,
                validFrom = null,
            )
        val patch =
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
            ).toPatch()
        patches.createPatch(
            patch.toSavedVariableDefinition(
                latestPatchOnValidityPeriod.patchId,
                latestPatchOnValidityPeriod,
            ),
            latestPatchOnValidityPeriod,
        )
        val validityPeriods = validityPeriods.list(INCOME_TAX_VP1_P1.definitionId)
        assertThat(validityPeriods[0].patchId).isEqualTo(8)
        assertThat(validityPeriods[1].patchId).isEqualTo(9)
        assertThat(validityPeriods[0].unitTypes).isNotEqualTo(validityPeriods[1].unitTypes)
    }

    @Test
    fun `create patch update owner and name on spesific period`() {
        val latestPatchOnValidityPeriod =
            validityPeriods.getMatchingOrLatest(
                INCOME_TAX_VP1_P1.definitionId,
                validFrom = LocalDate.of(1980, 1, 1),
            )
        val patch =
            INCOME_TAX_VP2_P6.copy(
                name =
                    LanguageStringType(
                        nb = "Dødvekt",
                        nn = "Dødvekt",
                        en = "Dead weight",
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
            ).toPatch()
        patches.createPatch(
            patch.toSavedVariableDefinition(
                latestPatchOnValidityPeriod.patchId,
                latestPatchOnValidityPeriod,
            ),
            latestPatchOnValidityPeriod,
        )
        val validityPeriods = validityPeriods.list(INCOME_TAX_VP1_P1.definitionId)
        assertThat(validityPeriods[0].patchId).isEqualTo(9)
        assertThat(validityPeriods[1].patchId).isEqualTo(8)
        assertThat(validityPeriods[0].name.nb).isNotEqualTo(validityPeriods[1].name.nb)
    }

    @Test
    fun `create patch update name not owner`() {
        val latestPatchOnValidityPeriod =
            validityPeriods.getMatchingOrLatest(
                INCOME_TAX_VP1_P1.definitionId,
                validFrom = null,
            )
        val patch =
            INCOME_TAX_VP2_P6.copy(
                name =
                    LanguageStringType(
                        nb = "Dødvekt",
                        nn = "Dødvekt",
                        en = "Dead weight",
                    ),
            ).toPatch()
        patches.createPatch(
            patch.toSavedVariableDefinition(
                latestPatchOnValidityPeriod.patchId,
                latestPatchOnValidityPeriod,
            ),
            latestPatchOnValidityPeriod,
        )
        val validityPeriods = validityPeriods.list(INCOME_TAX_VP1_P1.definitionId)
        assertThat(validityPeriods[0].patchId).isEqualTo(7)
        assertThat(validityPeriods[1].patchId).isEqualTo(8)
        assertThat(validityPeriods[0].name.nb).isNotEqualTo(validityPeriods[1].name.nb)
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

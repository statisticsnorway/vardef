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
    fun `create patch update owner team`(){
        val latestPatches = validityPeriods.listComplete(INCOME_TAX_VP1_P1.definitionId)
        val patch =
            INCOME_TAX_VP2_P6.copy(
                owner =
                Owner(
                    "dapla-felles",
                    listOf("pers-skatt-developers", TEST_DEVELOPERS_GROUP, "neighbourhood-dogs"
                    )),
            ).toPatch()
        val latestPatchOnValidityPeriod = validityPeriods.getMatchingOrLatest(
            INCOME_TAX_VP1_P1.definitionId, validFrom = null)
        patches.createPatch(patch.toSavedVariableDefinition(
            latestPatchOnValidityPeriod.patchId, latestPatchOnValidityPeriod),
            latestPatchOnValidityPeriod)
        val latestPatchesAfter = validityPeriods.listComplete(INCOME_TAX_VP1_P1.definitionId)
        assertThat(latestPatches).isNotEqualTo(latestPatchesAfter)
        assertThat(latestPatches[0].patchId).isEqualTo(latestPatchesAfter[0].patchId-1)
        assertThat(latestPatches[0].owner.team).isNotEqualTo(latestPatchesAfter[0].owner.team)
        assertThat(validityPeriods.list(INCOME_TAX_VP1_P1.definitionId)).isNull()
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

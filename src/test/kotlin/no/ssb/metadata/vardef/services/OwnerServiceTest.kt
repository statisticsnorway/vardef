package no.ssb.metadata.vardef.services

import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test


class OwnerServiceTest: BaseVardefTest() {
    @Inject
    private lateinit var ownerService: OwnerService

    @Inject
    private lateinit var validityPeriodsService: ValidityPeriodsService

    private val patch = Patch(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        Owner(
            team = "",
            groups = listOf("")
        ),
        null,
    )
    @Test
    fun `patch owner object`(){
        val validityPeriods = validityPeriodsService.listComplete(INCOME_TAX_VP1_P1.definitionId)
        val patchesBefore = validityPeriodsService.getAsMap(INCOME_TAX_VP1_P1.definitionId)

        val firstValiditPeriodNumPatches = patchesBefore[validityPeriods[0].validFrom]?.size
        val secondValiditPeriodNumPatches = patchesBefore[validityPeriods[1].validFrom]?.size

        val latestPatchIdBefore1 = validityPeriods[0].patchId
        val latestPatchIdBefore2 = validityPeriods[1].patchId

        val result = ownerService.patchOwner(INCOME_TAX_VP1_P1.definitionId, patch)

        assertThat(result.size).isEqualTo(validityPeriods.size)

        assertThat(result[0]?.patchId).isEqualTo(latestPatchIdBefore1 + 1)
        assertThat(result[1]?.patchId).isEqualTo(latestPatchIdBefore2 + 1)

        assertThat(result[0]?.validFrom).isEqualTo(validityPeriods[0].validFrom)

        val patchesAfter = validityPeriodsService.getAsMap(INCOME_TAX_VP1_P1.definitionId)

        if (firstValiditPeriodNumPatches != null) {
            assertThat(patchesAfter[validityPeriods[0].validFrom]).size().isEqualTo(firstValiditPeriodNumPatches + 1)
        }
        if (secondValiditPeriodNumPatches != null) {
            assertThat(patchesAfter[validityPeriods[1].validFrom]).size().isEqualTo(secondValiditPeriodNumPatches + 1)
        }
    }
}
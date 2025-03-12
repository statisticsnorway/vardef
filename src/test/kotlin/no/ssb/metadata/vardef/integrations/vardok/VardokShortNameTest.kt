package no.ssb.metadata.vardef.integrations.vardok

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class VardokShortNameTest: BaseVardefTest() {

    @Inject
    lateinit var vardokService: VardokService

    val possible_duplicates = listOf(
        566, 744, 1359, 1442, 1607, 1995, 2445, 2500, 2731, 3008, 3009, 3010,
        3011, 3047, 3049, 3310, 3391, 3398, 3448, 3449, 3453,500012
    )
    @Test
    fun `test duplicate name`(){
        assertThat(vardokService.isDuplicate("bus")).isTrue()
    }

    @Test
    fun `check dupliacte short name`() {
        val varDefInput = vardokService.fetchMultipleVardokItemsByLanguage("0005")
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.shortName).contains("generert")

    }
}
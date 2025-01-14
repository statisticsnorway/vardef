package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.integrations.vardok.utils.BaseVardokTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

class VardokInputTest : BaseVardokTest() {
    @Test
    fun `vardok items with missing data element name`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse6, "en" to vardokResponse8)
        val varDefInput = VardokService.extractVardefInput(mapVardokResponse)
        assertThat(varDefInput.shortName).startsWith("ugyldig_kortnavn")
    }

    @Test
    fun `vardok items with valid data element name`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse1)
        val varDefInput = VardokService.extractVardefInput(mapVardokResponse)
        assertThat(varDefInput.shortName).isEqualTo("r_dato")
    }

    @Test
    fun `vardok items with external document url`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse2)
        val varDefInput = VardokService.extractVardefInput(mapVardokResponse)
        assertThat(varDefInput.externalReferenceUri).isInstanceOf(URL::class.java)
    }
}

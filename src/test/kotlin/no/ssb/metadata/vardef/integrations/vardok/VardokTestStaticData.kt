package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.integrations.vardok.models.getValidDates
import no.ssb.metadata.vardef.integrations.vardok.models.mapVardokIdentifier
import no.ssb.metadata.vardef.integrations.vardok.services.VarDokApiService
import no.ssb.metadata.vardef.integrations.vardok.utils.BaseVardokTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

class VardokTestStaticData : BaseVardokTest() {
    @Test
    fun `parse valid date from`() {
        val validDates = getValidDates(vardokResponse3)
        val dateFrom = validDates.first
        val dateUntil = validDates.second
        assertThat(dateFrom).isNotNull
        assertThat(dateFrom).isEqualTo("1997-09-01")
        assertThat(dateUntil).isNull()
    }

    @Test
    fun `parse valid date until`() {
        val validDates = getValidDates(vardokResponse4)
        val dateFrom = validDates.first
        val dateUntil = validDates.second
        assertThat(dateFrom).isNotNull
        assertThat(dateUntil).isEqualTo("2006-01-01")
    }

    @Test
    fun `parse data element name to short name`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse2)
        val migrateVardokToVardef = VarDokApiService.extractVardefInput(mapVardokResponse)
        assertThat(migrateVardokToVardef.shortName).isEqualTo(vardokResponse2.variable?.dataElementName)
    }

    @Test
    fun `parse vardok id`() {
        val id = mapVardokIdentifier(vardokResponse3)
        assertThat(id).isEqualTo("476")
    }
}

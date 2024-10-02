package no.ssb.metadata.vardef.integrations.vardok

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
        val migrateVardokToVardef = toVarDefFromVarDok(mapVardokResponse)
        assertThat(migrateVardokToVardef.shortName).isEqualTo(vardokResponse2.variable?.dataElementName)
    }

    @Test
    fun `parse contact division to owner`() {
        val owner = mapVardokContactDivisionToOwner(vardokResponse1)
        assertThat(owner).isNotNull
        assertThat(owner.team).isEqualTo(vardokResponse1.common?.contactDivision?.codeValue)
        assertThat(owner.name).isEqualTo(vardokResponse1.common?.contactDivision?.codeText)
    }

    @Test
    fun `parse vardok id`() {
        val id = mapVardokIdentifier(vardokResponse3)
        assertThat(id).isEqualTo("476")
    }
}

package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.utils.BaseVardokTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConvertionsTest : BaseVardokTest() {
    @Test
    fun `convert statistical unit to unit types`()  {
        val statisticalUnit = "Offentlig forvaltning"
        val convertedValue = convertUnitTypes(statisticalUnit)
        assertThat(convertedValue).isEqualTo(listOf("22"))
    }
}

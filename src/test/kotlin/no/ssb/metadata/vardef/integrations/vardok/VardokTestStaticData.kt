package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.clearAllMocks
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VardokTestStaticData {
    private lateinit var vardokResponse1: VardokResponse
    private lateinit var vardokResponse2: VardokResponse
    private lateinit var vardokResponse3: VardokResponse
    private lateinit var vardokResponse4: VardokResponse
    private lateinit var vardokResponse5: VardokResponse
    private lateinit var vardokResponse6: VardokResponse

    @BeforeEach
    fun setUp() {
        val xmlMapper = XmlMapper().registerKotlinModule()
        vardokResponse1 = xmlMapper.readValue(vardokId1466validFromDateAndOtherLanguages, VardokResponse::class.java)
        vardokResponse2 = xmlMapper.readValue(vardokId49validUntilDate)
        vardokResponse3 = xmlMapper.readValue(vardokId476validFromDateAndNNInOtherLanguages)
        vardokResponse4 = xmlMapper.readValue(vardokId120validUntilDateAndOtherLanguages)
        vardokResponse5 = xmlMapper.readValue(vardokId100NoValidDates)
        vardokResponse6 = xmlMapper.readValue(vardokId123NoDataElementName)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

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
        assertThat(owner.code).isEqualTo(vardokResponse1.common?.contactDivision?.codeValue)
        assertThat(owner.name).isEqualTo(vardokResponse1.common?.contactDivision?.codeText)
    }

    @Test
    fun `parse vardok id`() {
        val id = mapVardokIdentifier(vardokResponse3)
        assertThat(id).isEqualTo("476")
    }
}

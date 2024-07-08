package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VardokServiceTest {
    private lateinit var varDokClient: VarDokClient
    private lateinit var varDokService: VarDokService
    private lateinit var vardokResponse1: VardokResponse
    private lateinit var vardokResponse2: VardokResponse
    private lateinit var vardokResponse3: VardokResponse
    private lateinit var vardokResponse4: VardokResponse
    private lateinit var vardokResponse5: VardokResponse
    private lateinit var vardokResponse6: VardokResponse

    @BeforeEach
    fun setUp() {
        varDokClient = mockk<VarDokClient>(relaxed = true)
        varDokService = VarDokService(varDokClient)
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
    fun `get vardok with valid and data element name returns 200 OK`() {
        every {
            varDokClient.fetchVarDokById("1466")
        } returns
            vardokResponse1
        val result = varDokService.getVarDokItem("1466")
        assertThat(result).isEqualTo(vardokResponse1)
    }

    @Test
    fun `get vardok with valid end date returns 200 OK`() {
        every {
            varDokClient.fetchVarDokById("49")
        } returns
            vardokResponse2
        val result = varDokService.getVarDokItem("49")
        assertThat(result).isEqualTo(vardokResponse2)
        assertThat(result?.dc?.valid).hasSizeGreaterThan(13)
    }

    @Test
    fun `get vardok with invalid id`() {
        every {
            varDokClient.fetchVarDokById("1")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Id not found")
        assertThrows(HttpStatusException::class.java) {
            varDokService.getVarDokItem("1")
        }
    }
}

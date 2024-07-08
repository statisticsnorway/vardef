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
    private lateinit var varDokMockkService: VarDokService
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
        varDokMockkService = mockk<VarDokService>(relaxed = true)
        val xmlMapper = XmlMapper().registerKotlinModule()
        vardokResponse1 = xmlMapper.readValue(vardokId1466validFromDateAndOtherLanguages)
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
    fun `get vardok with valid and data element name`() {
        every {
            varDokClient.fetchVarDokById("1466")
        } returns
            vardokResponse1
        val result = varDokService.getVarDokItem("1466")
        assertThat(result).isEqualTo(vardokResponse1)
    }

    @Test
    fun `get vardok by id and language nn language`() {
        every {
            varDokClient.fetchVarDokByIdAndLanguage("476", "nn")
        } returns
            vardokResponse3
        val result = varDokService.getVardokByIdAndLanguage("476", "nn")
        assertThat(result).isEqualTo(vardokResponse3)
        assertThat(result?.otherLanguages).isEqualTo("nn;en")
    }

    @Test
    fun `get vardok by valid id and not valid nn language returns nb language`() {
        val resultNNLanguage = varDokService.getVardokByIdAndLanguage("1466", "nn")
        val resultNBLanguage = varDokService.getVardokByIdAndLanguage("1466", "nb")
        assertThat(resultNNLanguage?.common?.title).isEqualTo(resultNBLanguage?.common?.title)
    }

    @Test
    fun `fetch multiple languages`() {
        val result = varDokService.fetchMultipleVarDokItemsByLanguage("476")
        assertThat(result).isInstanceOf(MutableMap::class.java)
    }

    @Test
    fun `get vardok with valid end date returns VardokResponse`() {
        every {
            varDokClient.fetchVarDokById("49")
        } returns
            vardokResponse2
        val result = varDokService.getVarDokItem("49")
        assertThat(result).isInstanceOf(VardokResponse::class.java)
        assertThat(result).isEqualTo(vardokResponse2)
        assertThat(result?.dc?.valid).hasSizeGreaterThan(13)
    }

    @Test
    fun `get vardok with invalid id`() {
        every {
            varDokClient.fetchVarDokById("1")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Id 1 not found")
        val exception: Exception =
            assertThrows(HttpStatusException::class.java) {
                varDokService.getVarDokItem("1")
            }
        val expectedMessage = "Id 1 not found"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }

    @Test
    fun `get vardok with missing valid`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse5)
        every {
            varDokMockkService.createVarDefInputFromVarDokItems(mapVardokResponse)
        } throws
            MissingValidDatesException()

        val exception: VardokException =
            assertThrows(MissingValidDatesException::class.java) {
                varDokService.createVarDefInputFromVarDokItems(mapVardokResponse)
            }
        val expectedMessage = "Vardok is missing valid dates and can not be saved"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }

    @Test
    fun `get vardok with missing data element name`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse6)
        every {
            varDokMockkService.createVarDefInputFromVarDokItems(mapVardokResponse)
        } throws
            MissingDataElementNameException()

        val exception: VardokException =
            assertThrows(MissingDataElementNameException::class.java) {
                varDokService.createVarDefInputFromVarDokItems(mapVardokResponse)
            }
        val expectedMessage = "Vardok is missing short name and can not be saved"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }
}

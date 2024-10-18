package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.services.VardokApiService
import no.ssb.metadata.vardef.integrations.vardok.services.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.utils.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VardokServiceTest : BaseVardokTest() {
    private lateinit var varDokMockkClient: VardokClient
    private lateinit var varDokApiService: VardokApiService
    private lateinit var varDokMockkService: VardokApiService

    @BeforeEach
    override fun setUp() {
        super.setUp()
        varDokMockkClient = mockk<VardokClient>(relaxed = true)
        varDokApiService = VardokApiService(varDokMockkClient)
        varDokMockkService = mockk<VardokApiService>(relaxed = true)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `get vardok with valid and data element name`() {
        every {
            varDokMockkClient.fetchVardokById("1466")
        } returns
            vardokResponse1
        val result = varDokApiService.getVardokItem("1466")
        assertThat(result).isEqualTo(vardokResponse1)
    }

    @Test
    fun `get vardok by id and language nn language`() {
        every {
            varDokMockkClient.fetchVardokByIdAndLanguage("476", "nn")
        } returns
            vardokResponse3
        val result = varDokApiService.getVardokByIdAndLanguage("476", "nn")
        assertThat(result).isEqualTo(vardokResponse3)
        assertThat(result?.otherLanguages).isEqualTo("nn;en")
    }

    @Test
    fun `get vardok by id and language - invalid id`() {
        every {
            varDokMockkClient.fetchVardokByIdAndLanguage("2990", "nb")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Id 2990 in language: nb not found")
        val exception: Exception =
            assertThrows(HttpStatusException::class.java) {
                varDokApiService.getVardokByIdAndLanguage("2990", "nb")
            }
        val expectedMessage = "Id 2990 in language: nb not found"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }

    @Test
    fun `get vardok by valid id and not valid nn language returns nb language`() {
        val resultNNLanguage = varDokApiService.getVardokByIdAndLanguage("1466", "nn")
        val resultNBLanguage = varDokApiService.getVardokByIdAndLanguage("1466", "nb")
        assertThat(resultNNLanguage?.common?.title).isEqualTo(resultNBLanguage?.common?.title)
    }

    @Test
    fun `fetch multiple languages`() {
        val result = varDokApiService.fetchMultipleVardokItemsByLanguage("476")
        assertThat(result).isInstanceOf(MutableMap::class.java)
    }

    @Test
    fun `get vardok with valid end date returns VardokResponse`() {
        every {
            varDokMockkClient.fetchVardokById("49")
        } returns
            vardokResponse2
        val result = varDokApiService.getVardokItem("49")
        assertThat(result).isInstanceOf(VardokResponse::class.java)
        assertThat(result).isEqualTo(vardokResponse2)
        assertThat(result?.dc?.valid).hasSizeGreaterThan(13)
    }

    @Test
    fun `get vardok with invalid id`() {
        every {
            varDokMockkClient.fetchVardokById("1")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Vardok id 1 not found")
        val exception: Exception =
            assertThrows(VardokNotFoundException::class.java) {
                varDokApiService.getVardokItem("1")
            }
        val expectedMessage = "Vardok id 1 not found"
        val actualMessage = exception.message

        assertThat(actualMessage).contains(expectedMessage)
    }

    @Test
    fun `get vardok with missing valid`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse5)
        every {
            varDokMockkService.createVarDefInputFromVarDokItems(mapVardokResponse)
        } throws
            MissingValidDatesException(mapVardokResponse["nb"]?.id.toString())

        val exception: VardokException =
            assertThrows(MissingValidDatesException::class.java) {
                varDokApiService.createVarDefInputFromVarDokItems(mapVardokResponse)
            }
        val expectedMessage = "Vardok id 100 is missing Valid (valid dates) and can not be saved"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }



    @Test
    fun `get vardok with missing data element name`() {
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse6)
        every {
            varDokMockkService.createVarDefInputFromVarDokItems(mapVardokResponse)
        } throws
            MissingDataElementNameException(mapVardokResponse["nb"]?.id.toString())

        val exception: VardokException =
            assertThrows(MissingDataElementNameException::class.java) {
                varDokApiService.createVarDefInputFromVarDokItems(mapVardokResponse)
            }
        val expectedMessage = "Vardok id 123 is missing DataElementName (short name) and can not be saved"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }
}

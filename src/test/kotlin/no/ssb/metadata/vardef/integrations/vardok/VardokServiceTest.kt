package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import no.ssb.metadata.vardef.integrations.vardok.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VardokServiceTest : BaseVardokTest() {
    private lateinit var varDokMockkClient: VarDokClient
    private lateinit var varDokService: VarDokService
    private lateinit var varDokMockkService: VarDokService

    @BeforeEach
    override fun setUp() {
        super.setUp()
        varDokMockkClient = mockk<VarDokClient>(relaxed = true)
        varDokService = VarDokService(varDokMockkClient)
        varDokMockkService = mockk<VarDokService>(relaxed = true)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `get vardok with valid and data element name`() {
        every {
            varDokMockkClient.fetchVarDokById("1466")
        } returns
            vardokResponse1
        val result = varDokService.getVarDokItem("1466")
        assertThat(result).isEqualTo(vardokResponse1)
    }

    @Test
    fun `get vardok by id and language nn language`() {
        every {
            varDokMockkClient.fetchVarDokByIdAndLanguage("476", "nn")
        } returns
            vardokResponse3
        val result = varDokService.getVardokByIdAndLanguage("476", "nn")
        assertThat(result).isEqualTo(vardokResponse3)
        assertThat(result?.otherLanguages).isEqualTo("nn;en")
    }

    @Test
    fun `get vardok by id and language - invalid id`() {
        every {
            varDokMockkClient.fetchVarDokByIdAndLanguage("2990", "nb")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Id 2990 in language: nb not found")
        val exception: Exception =
            assertThrows(HttpStatusException::class.java) {
                varDokService.getVardokByIdAndLanguage("2990", "nb")
            }
        val expectedMessage = "Id 2990 in language: nb not found"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
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
            varDokMockkClient.fetchVarDokById("49")
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
            varDokMockkClient.fetchVarDokById("1")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Vardok id 1 not found")
        val exception: Exception =
            assertThrows(VardokNotFoundException::class.java) {
                varDokService.getVarDokItem("1")
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
                varDokService.createVarDefInputFromVarDokItems(mapVardokResponse)
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
                varDokService.createVarDefInputFromVarDokItems(mapVardokResponse)
            }
        val expectedMessage = "Vardok id 123 is missing DataElementName (short name) and can not be saved"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }

    @Test
    fun `extract vardef input statistical unit to unit types`() {
        assertThat(vardokResponse7.variable?.statisticalUnit).isEqualTo("Avløpsanlegg")
        val mapVardokResponse: MutableMap<String, VardokResponse> = mutableMapOf("nb" to vardokResponse7)
        val vardefInput = VarDokService.extractVardefInput(mapVardokResponse)
        assertThat(vardefInput.unitTypes).isEqualTo(listOf("12", "13"))
    }
}

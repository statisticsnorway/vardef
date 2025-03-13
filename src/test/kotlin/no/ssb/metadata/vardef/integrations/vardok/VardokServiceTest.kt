package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.integrations.vardok.services.VardokApiService
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.integrations.vardok.utils.*
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VardokServiceTest : BaseVardokTest() {
    @MockK
    lateinit var vardokMockkClient: VardokClient

    @MockK
    lateinit var vardokMockkApiService: VardokApiService

    @MockK
    lateinit var vardokIdMappingRepository: VardokIdMappingRepository // is necessary dependency

    @MockK
    lateinit var variableDefinitionRepository: VariableDefinitionRepository

    @InjectMockKs
    lateinit var vardokApiService: VardokApiService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `get vardok with valid and data element name`() {
        every {
            vardokMockkClient.fetchVardokById("1466")
        } returns
            vardokId1466validFromDateAndOtherLanguages
        val result = vardokApiService.getVardokItem("1466")
        assertThat(result).isEqualTo(vardokResponse1)
    }

    @Test
    fun `get vardok by id and language nn language`() {
        every {
            vardokMockkClient.fetchVardokByIdAndLanguage("476", "nn")
        } returns
            vardokId476validFromDateAndNNInOtherLanguages
        val result = vardokApiService.getVardokByIdAndLanguage("476", "nn")
        assertThat(result).isEqualTo(vardokResponse3)
        assertThat(result?.otherLanguages).isEqualTo("nn;en")
    }

    @Test
    fun `get vardok by id and language - invalid id`() {
        every {
            vardokMockkClient.fetchVardokByIdAndLanguage("2990", "nb")
        } throws
            HttpStatusException(HttpStatus.NOT_FOUND, "Id 2990 in language: nb not found")
        val exception: Exception =
            assertThrows(HttpStatusException::class.java) {
                vardokApiService.getVardokByIdAndLanguage("2990", "nb")
            }
        val expectedMessage = "Id 2990 in language: nb not found"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }

    @Test
    fun `get vardok with valid end date returns VardokResponse`() {
        every {
            vardokMockkClient.fetchVardokById("49")
        } returns
            vardokId49validUntilDate
        val result = vardokApiService.getVardokItem("49")
        assertThat(result).isInstanceOf(VardokResponse::class.java)
        assertThat(result).isEqualTo(vardokResponse2)
        assertThat(result?.dc?.valid).hasSizeGreaterThan(13)
    }

    @Test
    fun `get vardok with invalid id`() {
        every {
            vardokMockkClient.fetchVardokById("1")
        } throws
            VardokNotFoundException("Vardok id 1 not found")
        val exception: Exception =
            assertThrows(VardokNotFoundException::class.java) {
                vardokApiService.getVardokItem("1")
            }
        val expectedMessage = "Vardok id 1 not found"
        val actualMessage = exception.message

        assertThat(actualMessage).contains(expectedMessage)
    }

    @Test
    fun `short name exist`() {
        every {
            variableDefinitionRepository.existsByShortName("fnr")
        } returns
            true
        val result = vardokApiService.isDuplicate("fnr")
        assertThat(result).isTrue()
    }

    @Test
    fun `duplicate short name`() {
        val variableMock = mockk<Variable>(relaxed = true)
        every { variableMock.dataElementName } returns "Adressenavn"
        every { variableMock.dataElementName = any() } answers { callOriginal() }
        val vardokResponse = mockk<VardokResponse>(relaxed = true)
        every { vardokResponse.variable } returns variableMock
        every { vardokMockkApiService.getVardokItem("100") } returns vardokResponse
        every {
            vardokMockkClient.fetchVardokById("100")
        } returns
            vardokId100NoValidDates
        every {
            vardokApiService.isDuplicate("Adressenavn")
        } returns
            true
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("100")
        val varDefInput = VardokService.extractVardefInput(result)
        assertThat(varDefInput.shortName).contains("generert_")
    }
}

package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.repositories.VardokIdMappingRepository
import no.ssb.metadata.vardef.integrations.vardok.services.VardokApiService
import no.ssb.metadata.vardef.integrations.vardok.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VardokServiceTest : BaseVardokTest() {
    private lateinit var vardokMockkClient: VardokClient
    private lateinit var vardokApiService: VardokApiService
    private lateinit var vardokMockkService: VardokApiService

    @Inject
    private lateinit var vardokIdMappingRepository: VardokIdMappingRepository

    @BeforeEach
    override fun setUp() {
        super.setUp()
        vardokMockkClient = mockk<VardokClient>(relaxed = true)
        vardokApiService = VardokApiService(vardokMockkClient, vardokIdMappingRepository)
        vardokMockkService = mockk<VardokApiService>(relaxed = true)
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
            HttpStatusException(HttpStatus.NOT_FOUND, "Vardok id 1 not found")
        val exception: Exception =
            assertThrows(VardokNotFoundException::class.java) {
                vardokApiService.getVardokItem("1")
            }
        val expectedMessage = "Vardok id 1 not found"
        val actualMessage = exception.message

        assertThat(actualMessage).contains(expectedMessage)
    }
}

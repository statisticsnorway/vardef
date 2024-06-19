package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.http.server.exceptions.HttpServerException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

@MockK
class KlassApiServiceMockkTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiService: KlassApiService
    private lateinit var klassApiMockService: KlassApiService
    private lateinit var klassApiResponse: KlassApiResponse

    @BeforeEach
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>(relaxed = true)
        klassApiService = KlassApiService(klassApiMockkClient)
        klassApiMockService = mockk<KlassApiService>()
        klassApiResponse = mockk<KlassApiResponse>()
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `klass api job`() {
        every { klassApiMockkClient.fetchClassifications() } returns (klassApiResponse)
        klassApiService.fetchAllClassifications()
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `no response klass api returns exception`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } throws IOException("Error while fetching classifications from Klass Api")
        klassApiService.fetchAllClassifications()
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `server error klass api`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } throws HttpServerException("Server error")
        val result = klassApiService.getClassifications()
        assertThat(result).isNotNull
    }

    @Test
    fun `klass api status ok`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns klassApiResponse
        val response = klassApiService.getClassifications()
        assertThat(response).isNotNull
    }
}

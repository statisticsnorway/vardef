package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.http.HttpStatus
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
class KlassApiMockkTest {
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
        every { klassApiMockkClient.fetchClassificationList() } returns (klassApiResponse)
        val jobResult = klassApiService.fetchClassifications()
        assertThat(jobResult).isNotNull
        assertThat(jobResult.status).isEqualTo(HttpStatus.OK)
        verify(exactly = 1) { klassApiMockkClient.fetchClassificationList() }
    }

    @Test
    fun `no response klass api returns exception`() {
        every {
            klassApiMockkClient.fetchClassificationList()
        } throws IOException("Error while fetching classifications from Klass Api")
        val result = klassApiService.fetchClassifications()
        assertThat(result.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) { klassApiMockkClient.fetchClassificationList() }
    }

    @Test
    fun `klass api job returns status`() {
        every {
            klassApiMockkClient.fetchClassificationList()
        } throws HttpServerException("Server error")
        val result = klassApiService.fetchClassifications()
        assertThat(result.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @Test
    fun `server error klass api`() {
        every {
            klassApiMockkClient.fetchClassificationList()
        } throws HttpServerException("Server error")
        val result = klassApiService.getClassifications()
        assertThat(result).isNull()
    }

    @Test
    fun `klass api status ok`() {
        every {
            klassApiMockkClient.fetchClassificationList()
        } returns klassApiResponse
        val response = klassApiService.getClassifications()
        assertThat(response).isNotNull
        assertThat(response).isInstanceOf(KlassApiResponse::class.java)
    }
}

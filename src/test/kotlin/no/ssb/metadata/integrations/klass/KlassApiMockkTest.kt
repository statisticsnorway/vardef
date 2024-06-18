package no.ssb.metadata.integrations.klass

import io.micronaut.http.server.exceptions.HttpServerException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
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
        every { klassApiMockkClient.fetchClassifications() } returns (klassApiResponse)
        val jobResult = klassApiService.getClassifications()
        assertThat(jobResult).isNotNull
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `no response klass api returns exception`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } throws IOException("Error while fetching classifications from Klass Api")
        val result = klassApiService.getClassifications()
        assertThat(result).isNull()
        verify(exactly = 1) { klassApiMockkClient.fetchClassifications() }
    }

    @Test
    fun `klass api job returns status`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } throws HttpServerException("Server error")
        val result = klassApiService.getClassifications()
        assertThat(result).isNull()
    }

    @Test
    fun `server error klass api`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } throws HttpServerException("Server error")
        val result = klassApiService.getClassifications()
        assertThat(result).isNull()
    }

    @Test
    fun `klass api status ok`() {
        every {
            klassApiMockkClient.fetchClassifications()
        } returns klassApiResponse
        val response = klassApiService.getClassifications()
        assertThat(response).isNotNull
        assertThat(response).isInstanceOf(KlassApiResponse::class.java)
        assertThat(klassApiService.klassApiResponse).isNotNull
    }
}

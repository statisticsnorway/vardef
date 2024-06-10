package no.ssb.metadata.integrations.klass

import io.micronaut.http.HttpStatus
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class KlassApiJobTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiService: KlassApiService

    @BeforeEach
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>(relaxed = true)
        klassApiService = KlassApiService(klassApiMockkClient)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `klass api job is run`() {
        val klassApiResponse = mockk<KlassApiResponse>()
        every { klassApiMockkClient.fetchClassificationList() } returns (klassApiResponse)
        val jobResult = klassApiService.klassApiJob()
        assertThat(jobResult).isNotNull
        assertThat(jobResult.status).isEqualTo(HttpStatus.OK)
        verify(exactly = 1) { klassApiMockkClient.fetchClassificationList() }
    }

    /*@Test
    fun `no response klass api returns exception`() {
        every {
            klassApiMockkClient.fetchClassificationList()
         } throws Exception()

        19:36:13.142 [Test worker] WARN  n.s.m.v.i.k.service.KlassApiService - Error while fetching classifications from Klass Api
java.lang.Exception: null


        val result = klassApiService.klassApiJob()
        assertThat(result.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) { klassApiMockkClient.fetchClassificationList() }
    }*/
}

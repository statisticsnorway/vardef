package no.ssb.metadata.integrations.klass

import io.micronaut.http.HttpStatus
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse
import no.ssb.metadata.vardef.integrations.klass.scheduling.KlassApiJob
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class KlassApiJobTest {
    private lateinit var klassApiMockkClient: KlassApiClient
    private lateinit var klassApiJob: KlassApiJob

    @BeforeEach
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>(relaxed = true)
        klassApiJob = KlassApiJob(klassApiMockkClient)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `Job is run`() {
        val klassApiResponse = mockk<KlassApiResponse>()
        every { klassApiMockkClient.fetchClassificationList() } returns (klassApiResponse)
        val jobResult = klassApiJob.getClassifications()
        assertThat(jobResult).isNotNull
        assertThat(jobResult.status).isEqualTo(HttpStatus.OK)
        verify(exactly = 1) { klassApiMockkClient.fetchClassificationList() }
    }

    @Test
    fun `no response returns exception`() {
        every {
            klassApiMockkClient.fetchClassificationList()
        } throws Exception("test")
        val result = klassApiJob.getClassifications()
        assertThat(result.status).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) { klassApiMockkClient.fetchClassificationList() }
    }
}

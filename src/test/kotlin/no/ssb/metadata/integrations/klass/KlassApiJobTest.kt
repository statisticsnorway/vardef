package no.ssb.metadata.integrations.klass

import io.micronaut.http.HttpResponse
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.scheduling.KlassApiJob
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class KlassApiJobTest {
    private lateinit var klassApiMockkClient: KlassApiClient

    @BeforeEach
    fun setUp() {
        klassApiMockkClient = mockk<KlassApiClient>(relaxed = true)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `Job is run`() {
        val job = mockk<KlassApiJob>(relaxed = true)
        val result = klassApiMockkClient.fetchClassificationList()
        every {
            job.getClassifications()
        } returns HttpResponse.ok(result)
        job.getClassifications()
        verify(exactly = 1) { job.getClassifications() }
    }

    @Test
    fun `no response returns exception`() {
        val job = mockk<KlassApiJob>(relaxed = true)
        every {
            job.getClassifications()
        } returns HttpResponse.notFound()
        job.getClassifications()
        verify(exactly = 1) { job.getClassifications() }
    }
}

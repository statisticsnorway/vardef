package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import no.ssb.metadata.vardef.integrations.klass.scheduling.KlassApiCacheJob
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(startApplication = false)
class KlassApiCacheTest {
    @Inject
    lateinit var klassApiService: KlassApiService
    private lateinit var klassApiCacheJob: KlassApiCacheJob

    @Timeout(4)
    @Test
    @Order(1)
    fun `first run cache`() {
        assertThat(klassApiService.classifications).isNull()
        klassApiCacheJob = KlassApiCacheJob(klassApiService)
        klassApiCacheJob.runDailyKlassApiJob()
        assertThat(klassApiService.classifications).isNotNull
    }

    @Timeout(1000)
    @Test
    @Order(2)
    fun `second run cache`() {
        klassApiCacheJob = KlassApiCacheJob(klassApiService)
        assertThat(klassApiService.classifications).isNotNull()
        val result = klassApiCacheJob.getClassificationsFromCache()
        assertThat(klassApiService.classifications).isEqualTo(result)
        assertThat(klassApiService.classifications?.embedded).isInstanceOf(Classifications::class.java)
    }
}

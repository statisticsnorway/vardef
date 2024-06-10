package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(startApplication = false)
class KlassApiCacheTest {
    @Inject
    lateinit var klassApiService: KlassApiService

    @Timeout(4)
    @Test
    @Order(1)
    fun `first run cache`() {
        assertThat(klassApiService.klassApiResponse).isNull()
        klassApiService.klassApiJob()
        assertThat(klassApiService.klassApiResponse).isNotNull()
    }

    @Timeout(1000)
    @Test
    @Order(2)
    fun `second run cache`() {
        assertThat(klassApiService.klassApiResponse).isNotNull()
        assertThat(klassApiService.klassApiResponse).isEqualTo(klassApiService.getClassifications())
        assertThat(klassApiService.klassApiResponse?.embedded).isInstanceOf(Classifications::class.java)
    }

    @Timeout(100)
    @Test
    @Order(3)
    fun `third run cache`() {
        val result = klassApiService.getClassifications()
        assertThat(result).isNotNull()
    }
}

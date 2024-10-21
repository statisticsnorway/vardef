package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(startApplication = false)
@Requires(env = ["integration-test"])
class KlassApiServiceIntegrationTest {
    @Inject
    lateinit var klassApiService: KlassApiService

    @Test
    fun `fetch existing classification by id from klass api`() {
        val result = klassApiService.getClassification(702)
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo("Kodeliste for enhetstyper")
    }

    @Test
    fun `fetch NON-existing classification by id from klass api`() {
        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(0)
        }
    }
}

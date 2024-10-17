package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
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

    @Property(name = "klass.cached-classifications.unit-types")
    private var unitTypesId: Int = 0

    @Test
    fun `fetch existing classification by id from klass api`() {
        val result = klassApiService.getClassification(unitTypesId)
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo("Enhetstyper")

//        val classificationList = result.codes
//        assertThat(classificationList[0]).isInstanceOf(Code::class.java)
    }

    @Test
    fun `fetch NON-existing classification by id from klass api`() {
        assertThrows<NoSuchElementException> {
            klassApiService.getClassification(0)
        }
    }
}

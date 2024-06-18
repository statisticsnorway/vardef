package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(startApplication = false)
class KlassApiServiceTest {
    @Inject
    lateinit var klassApiService: KlassApiService
    @Property(name = "klass.cached-classifications.unit-types")
    private var unitTypesId: Int = 0


    @Timeout(4)
    @Test
    @Order(1)
    fun `first run cache`() {
        assertThat(klassApiService.klassApiResponse).isNull()
        klassApiService.fetchClassifications()
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

    @Test
    fun `fetch existing classification by id from klass api`() {
        val result = klassApiService.getClassification(unitTypesId)
        assertThat(result).isNotNull

        val classificationList = result?.classificationItems ?: emptyList()
        assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
    }

    @Test
    fun `fetch NON-existing classification by id from klass api`() {
        val result = klassApiService.getClassification(0)
        assertThat(result).isNull()
    }

    @Test
    fun `get unit types from klass api`() {
        val result = klassApiService.getUnitTypes()
        assertThat(result).isNotNull

        val classificationList = result?.classificationItems ?: emptyList()
        assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
    }

    @Test
    fun `get areas from klass api`() {
        val result = klassApiService.getAreas()
        assertThat(result).isNotNull

        val classificationList = result?.classificationItems ?: emptyList()
        assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
    }

}

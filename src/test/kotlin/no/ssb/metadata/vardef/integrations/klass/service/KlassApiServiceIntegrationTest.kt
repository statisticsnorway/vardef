package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@MicronautTest(startApplication = false)
class KlassApiServiceIntegrationTest {
    @Inject
    lateinit var klassApiService: KlassApiService

    @Property(name = "klass.cached-classifications.unit-types")
    private var unitTypesId: Int = 0

    @Test
    fun `fetch existing classification by id from klass api`() {
        val result = klassApiService.getClassification(unitTypesId)
        assertThat(result).isNotNull

        val classificationList = result.classificationItems
        assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
    }

    @Test
    fun `fetch NON-existing classification by id from klass api`() {
        val result = klassApiService.getClassification(0)
        assertThat(result).isNotNull
        assertEquals("", result.name)

        val classificationList = result.classificationItems
        assertThat(classificationList.isEmpty())
    }
}

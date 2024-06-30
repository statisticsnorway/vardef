package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiClientIntegrationTest {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    @Property(name = "klass.cached-classifications.unit-types")
    private val unitTypesId: Int = 702

    @Property(name = "klass.cached-classifications.areas")
    private val areasId: Int = 618

    @Test
    fun `fetch classifications from klass api`() {
        val result = klassApiClient.fetchClassifications()
        assertThat(result).isNotNull
        assertThat(result.body()?.embedded).isNotNull
        assertThat(result.body()?.embedded).isInstanceOf(Classifications::class.java)
        assertThat(result.body()?.embedded?.classifications).isNotNull

        val classificationList = result.body()?.embedded?.classifications
        assertThat(classificationList?.get(0) ?: emptyList<Classification>()).isInstanceOf(Classification::class.java)
    }

    @Test
    fun `fetch code list from klass api`() {
        listOf(unitTypesId, areasId)
            .forEach { id ->
                val result = klassApiClient.fetchCodeList(id)
                assertThat(result).isNotNull

                val classificationList = result.body()?.classificationItems ?: emptyList()
                assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
                assertThat(classificationList.size > 1)
            }
    }

    @Test
    fun `fetch classification from klass api`() {
        listOf(unitTypesId, areasId)
            .forEach { id ->
                val result = klassApiClient.fetchClassification(id)
                assertThat(result).isNotNull
                assertThat(id == result.body()?.id)
            }
    }
}

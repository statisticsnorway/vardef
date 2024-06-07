package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem
import no.ssb.metadata.vardef.integrations.klass.models.Classifications
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiResponseTest {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    @Test
    fun `request data klass api`(spec: RequestSpecification) {
        val result = klassApiClient.fetchClassificationList()
        val classificationList = result.embedded.classificationItems
        assertThat(result).isNotNull
        assertThat(result.page).isNotNull
        assertThat(classificationList.get(0)).isInstanceOf(ClassificationItem::class.java)
        assertThat(result.embedded).isInstanceOf(Classifications::class.java)
    }
}

package no.ssb.metadata.integrations.klass

import io.micronaut.context.annotation.Value
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.*
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.function.Supplier

// No available service exception

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiClientTest {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    @Value("\${klass.cached-classifications}")
    private lateinit var cachedClassifications: List<Map<String, String>>

    @Test
    fun `fetch classifications from klass api`() {
        val result = klassApiClient.fetchClassificationList()
        assertThat(result).isNotNull
        assertThat(result.page).isNotNull
        assertThat(result.links).isNotNull

        val classificationList = result.embedded.classifications
        assertThat(classificationList[0]).isInstanceOf(Classification::class.java)
        assertThat(result.embedded).isInstanceOf(Classifications::class.java)
        assertThat(result.links).isInstanceOf(PaginationLinks::class.java)
        assertThat(result.links.next).isInstanceOf(Link::class.java)
    }

    @Test
    fun `fetch code list from klass api`() {
        cachedClassifications
            .map { entry -> entry.getOrDefault("id", "0").toInt() }
            .forEach { id ->
                val result = klassApiClient.fetchCodeListAtDate(id, LocalDate.now())
                assertThat(result).isNotNull

                val classificationList = result?.classificationItems ?: emptyList()
                assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
                assertThat(classificationList.size > 1)
            }
    }

    @Test
    fun `fetch classification from klass api`() {
        cachedClassifications
            .map { entry -> entry.getOrDefault("id", "0").toInt() }
            .forEach { id ->
                val result = klassApiClient.fetchClassification(id)
                assertThat(result).isNotNull
                assertThat(id == result?.id)
            }
    }
}

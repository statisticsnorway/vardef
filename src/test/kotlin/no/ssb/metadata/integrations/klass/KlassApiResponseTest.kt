package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.models.*
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiClient
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

// No available service exception

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KlassApiResponseTest {
    @Inject
    lateinit var klassApiClient: KlassApiClient

    private lateinit var result: KlassApiResponse

    @BeforeAll
    fun setUp() {
        result = klassApiClient.fetchClassificationList()
    }

    @Test
    fun `request data klass api`() {
        assertThat(result).isNotNull
        assertThat(result.page).isNotNull
        assertThat(result.links).isNotNull
    }

    @Test
    fun `klass api result serialize to dataclasses`() {
        val classificationList = result.embedded.classificationItems
        assertThat(classificationList[0]).isInstanceOf(ClassificationItem::class.java)
        assertThat(result.embedded).isInstanceOf(Classifications::class.java)
        assertThat(result.links).isInstanceOf(PaginationLinks::class.java)
        assertThat(result.links.next).isInstanceOf(Link::class.java)
    }
}

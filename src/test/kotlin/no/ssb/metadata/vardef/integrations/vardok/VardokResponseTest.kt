package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
class VardokResponseTest {
    @Inject
    lateinit var vardokService: VardokService

    @Test
    fun `relations in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.relations).isNotNull()
        assertThat(response?.relations?.classificationRelation).isNull()
    }

    @Test
    fun `relations classificationRelation in response`() {
        val response = vardokService.getVardokItem("1919")
        assertThat(response?.relations?.classificationRelation).isNotNull()
    }
}

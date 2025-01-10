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
    fun `calculation in response`() {
        val response = vardokService.getVardokItem("566")
        assertThat(response?.variable?.calculation).isNotNull
    }

    @Test
    fun `relations in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.relations).isNotNull()
        assertThat(response?.relations?.classificationRelation).isNull()
    }

    @Test
    fun `calculation not in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.variable?.calculation).isEmpty()
    }

    @Test
    fun `relations classificationRelation in response`() {
        val response = vardokService.getVardokItem("1919")
        assertThat(response?.relations?.classificationRelation).isNotNull()
    }

    @Test
    fun `notes in response`() {
        val response = vardokService.getVardokItem("134")
        assertThat(response?.common?.notes).isEqualTo("Dokumentet det refereres til er \"Om statistikken\" som ligger på Internett.")
    }

    @Test
    fun `notes not in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.common?.notes).isEmpty()
    }
}

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
    fun `relations conceptVariableRelation list in response`() {
        val response = vardokService.getVardokItem("2")
        assertThat(response?.relations?.conceptVariableRelations).isNotNull()
        assertThat(response?.relations?.conceptVariableRelations?.size).isEqualTo(5)
        assertThat(response?.relations?.conceptVariableRelations).isInstanceOf(List::class.java)
    }

    @Test
    fun `relations conceptVariableRelation list not in response`() {
        val response = vardokService.getVardokItem("5")
        assertThat(response?.relations?.conceptVariableRelations).isNull()
    }
}

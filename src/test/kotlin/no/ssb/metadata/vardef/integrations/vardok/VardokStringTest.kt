package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.integrations.vardok.services.VardokApiService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

@Requires(env = ["integration-test"])
@MicronautTest
class VardokStringTest {
    @Inject
    lateinit var vardokClient: VardokClient

    @Inject
    lateinit var vardokApiService: VardokApiService

    val xmlMapper = XmlMapper().registerKotlinModule()

    @Test
    fun`get item`() {
        val res = vardokClient.fetchVardokById("90")
        val res2 = xmlMapper.readValue(res, VardokResponse::class.java)
        assertThat(res2.relations?.conceptVariableRelations?.size).isEqualTo(0)
        assertThat(res2.relations?.classificationRelation).isNull()
    }

    @Test
    fun`get item 2`() {
        val res = vardokClient.fetchVardokById("1919")
        val res2 = xmlMapper.readValue(res, VardokResponse::class.java)
        assertThat(res2.relations?.classificationRelation?.href).isEqualTo("http://www.ssb.no/classification/klass/91")
    }

    @Test
    fun`get item 3`() {
        val res = vardokClient.fetchVardokById("2")
        val res2 = xmlMapper.readValue(res, VardokResponse::class.java)
        assertThat(res2.relations?.classificationRelation).isNull()
        assertThat(res2.relations?.conceptVariableRelations?.size).isEqualTo(5)
    }

    @Test
    fun `get vardok by valid id and not valid nn language returns nb language`() {
        val resultNNLanguage = vardokApiService.getVardokByIdAndLanguage("1466", "nn")
        val resultNBLanguage = vardokApiService.getVardokByIdAndLanguage("1466", "nb")
        assertThat(resultNBLanguage).isNotNull
        assertThat(resultNNLanguage?.common?.title).isEqualTo(resultNBLanguage?.common?.title)
    }

    @Test
    fun `fetch multiple languages`() {
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("476")
        assertThat(result).isInstanceOf(MutableMap::class.java)
    }
}

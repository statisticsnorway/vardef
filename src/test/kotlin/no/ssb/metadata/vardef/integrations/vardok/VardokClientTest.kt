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
class VardokClientTest {
    @Inject
    lateinit var vardokClient: VardokClient

    @Inject
    lateinit var vardokApiService: VardokApiService

    private val xmlMapper = XmlMapper().registerKotlinModule()

    @Test
    fun`use xml mapper to read string http response vardok by id`() {
        val result = xmlMapper.readValue(vardokClient.fetchVardokById("90"), VardokResponse::class.java)
        assertThat(result).isInstanceOf(VardokResponse::class.java)
    }

    @Test
    fun `get vardok by valid id and not valid nn language returns nb language`() {
        val resultNN = xmlMapper.readValue(vardokClient.fetchVardokByIdAndLanguage("1466", "nn"), VardokResponse::class.java)
        val resultNB = xmlMapper.readValue(vardokClient.fetchVardokByIdAndLanguage("1466", "nb"), VardokResponse::class.java)
        assertThat("nn").isNotIn(resultNB.otherLanguages)
        assertThat(resultNN?.common?.title).isEqualTo(resultNB?.common?.title)
    }

    @Test
    fun `fetch multiple languages`() {
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("476")
        assertThat(result).isInstanceOf(MutableMap::class.java)
    }
}

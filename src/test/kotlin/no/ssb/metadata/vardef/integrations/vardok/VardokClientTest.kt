package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.context.annotation.Requires
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.client.VardokClient
import no.ssb.metadata.vardef.integrations.vardok.convertions.mapVardokStatisticalUnitToUnitTypes
import no.ssb.metadata.vardef.integrations.vardok.models.OutdatedUnitTypesException
import no.ssb.metadata.vardef.integrations.vardok.models.VardokNotFoundException
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse
import no.ssb.metadata.vardef.integrations.vardok.services.VardokApiService
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

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
    fun`use xml mapper to read string http response with external document`() {
        val result = xmlMapper.readValue(vardokClient.fetchVardokById("130"), VardokResponse::class.java)
        assertThat(result).isInstanceOf(VardokResponse::class.java)
        assertThat(result.variable?.externalDocument).isEqualTo("")
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

    @ParameterizedTest
    @ValueSource(
        ints = [
            1245, 1248, 1755, 1756, 2320, 2321, 2464, 2468, 2469, 2470, 2471, 2485, 2502, 2503, 2634, 2635, 2689, 2798,
            3119, 3330, 3354, 3359, 3360, 3428, 3431,
        ],
    )
    fun `fetch vardok with invalid external document`(id: Int) {
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("$id")
        assertThat(result).isInstanceOf(MutableMap::class.java)
        val varDefInput = VardokService.extractVardefInput(result)
        assertThat(varDefInput.externalReferenceUri).isNull()
    }

    @Test
    fun `fetch vardok with null external document`() {
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("130")
        assertThat(result).isInstanceOf(MutableMap::class.java)
        val varDefInput = VardokService.extractVardefInput(result)
        assertThat(varDefInput.externalReferenceUri).isNull()
    }

    @Test
    fun `special unit types convertion`() {
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("1416")
        val varDefInput = VardokService.extractVardefInput(result)
        assertThat(varDefInput.unitTypes).isEqualTo(listOf("20"))
    }

    @Test
    fun `special case unit types convertion list of multiple unit types`() {
        val result = vardokApiService.fetchMultipleVardokItemsByLanguage("2216")
        val varDefInput = VardokService.extractVardefInput(result)
        assertThat(varDefInput.unitTypes).isEqualTo(listOf("01", "04", "05"))
    }

    @Test
    fun `Vardok not found`() {
        assertThatThrownBy {
            vardokApiService.fetchMultipleVardokItemsByLanguage("21")
        }.isInstanceOf(VardokNotFoundException::class.java)
            .hasMessageContaining("Vardok id 21 not found")
    }
}

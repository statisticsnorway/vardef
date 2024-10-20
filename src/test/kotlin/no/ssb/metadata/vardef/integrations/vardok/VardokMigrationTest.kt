package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.integrations.vardok.utils.vardokId1466validFromDateAndOtherLanguages
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@MicronautTest
class VardokMigrationTest {
    @Inject
    lateinit var vardokService: VardokService

    @Test
    fun `get vardok by id`() {
        val result = vardokService.getVardokItem("901")
        assertThat(result).isNotNull()
        assertThat(result?.dc?.contributor).isEqualTo("Seksjon for befolkningsstatistikk")
        assertThat(result?.common?.title).isEqualTo("Oppvarming, har lukket ovn for fast brensel")
        assertThat(result?.otherLanguages).isEqualTo("en")
        assertThat(result?.type).isEqualTo("ConceptVariable")
        assertThat(result?.xmlLang).isEqualTo("nb")
    }

    @Test
    fun `get vardok by id and language if other languages`() {
        val res = vardokService.getVardokItem("901")
        var englishRes: VardokResponse? = null
        if (res?.otherLanguages != "") {
            englishRes = res?.let { vardokService.getVardokByIdAndLanguage("901", it.otherLanguages) }
        }
        assertThat(englishRes?.common?.title).isEqualTo("System for heating, has closed stoves for solid fuel")
        assertThat(englishRes?.id).isEqualTo(res?.id)
    }

    @Test
    fun `map vardok date from`() {
        val res = vardokService.getVardokItem("901")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(10)
        val mappedFromDate = res?.let { getValidDates(it).first }
        assertThat(mappedFromDate).isNotNull()
        assertThat(mappedFromDate).isEqualTo("2001-01-01")
    }

    @Test
    fun `map vardok date until`() {
        val res = vardokService.getVardokItem("901")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(20)
        val mappedUntilDate = res?.let { getValidDates(it).second }
        assertThat(mappedUntilDate).isNotNull()
        assertThat(mappedUntilDate).isEqualTo("2001-12-31")
    }

    @Test
    fun `map vardok missing valid date`() {
        val res = vardokService.getVardokItem("134")

        val exception: VardokException =
            assertThrows(MissingValidFromException::class.java) {
                if (res != null) {
                    getValidDates(res)
                }
            }
        assertThat(exception.message).isEqualTo("Vardok id 134 Valid is missing 'from' date and can not be saved")
    }

    @Test
    fun `map vardok missing valid end date`() {
        val res = vardokService.getVardokItem("1422")
        val mappedUntilDate = res?.let { getValidDates(it).second }
        assertThat(mappedUntilDate).isNull()
    }

    @ParameterizedTest
    @ValueSource(strings = ["1422", "1919", "2", "5", "123"])
    fun `set link to vardok`(vardokId: String) {
        val result = vardokService.getVardokItem(vardokId)
        if (result != null) {
            val mapResult: MutableMap<String, VardokResponse> = mutableMapOf("nb" to result)
            val renderVarDok = VardokService.extractVardefInput(mapResult)
            assertThat(renderVarDok).isNotNull
            assertThat(
                renderVarDok.externalReferenceUri,
            ).isEqualTo("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId")
        }
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            2, 5, 26, 120,
        ],
    )
    fun `map owner from vardok`(vardokId: Int) {
        val result = vardokService.getVardokItem(vardokId.toString())
        assertThat(result).isNotNull
        assertThat(result?.common?.contactDivision).isNotNull
        assertThat(result?.common?.contactDivision?.codeValue).isNotNull()
        assertThat(result?.common?.contactDivision?.codeText).isNotNull()
    }

    @Test
    fun `vardok item has not short name`() {
        val result = vardokService.getVardokItem("2450")
        if (result != null) {
            val mapResult: MutableMap<String, VardokResponse> = mutableMapOf("nb" to result)
            val exception: VardokException =
                assertThrows(VardokException::class.java) {
                    vardokService.createVarDefInputFromVarDokItems(mapResult)
                }
            assertThat(exception).isInstanceOf(VardokException::class.java)
            val expectedMessage = "Vardok id 2450 is missing DataElementName (short name) and can not be saved"
            val actualMessage = exception.message

            assertThat(expectedMessage).isEqualTo(actualMessage)
        }
    }

    @Test
    fun `vardok item has not valid dates`() {
        val result = vardokService.getVardokItem("100")
        if (result != null) {
            val mapResult: MutableMap<String, VardokResponse> = mutableMapOf("nb" to result)
            val exception: MissingValidDatesException =
                assertThrows(MissingValidDatesException::class.java) {
                    vardokService.createVarDefInputFromVarDokItems(mapResult)
                }
            assertThat(exception).isInstanceOf(MissingValidDatesException::class.java)
            val expectedMessage = "Vardok id 100 is missing Valid (valid dates) and can not be saved"
            val actualMessage = exception.message

            assertThat(expectedMessage).isEqualTo(actualMessage)
        }
    }

    @Test
    fun `data element name with uppercase`() {
        val vardok = vardokService.getVardokItem("130")
        assertThat(vardok?.variable?.dataElementName).isEqualTo("Ufg")
        val varDefInput = vardokService.fetchMultipleVardokItemsByLanguage("130")
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        val afterMigration = JSONObject(vardokTransform)
        assertThat(afterMigration["shortName"]).isEqualTo("ufg")
    }

    @ParameterizedTest
    @ValueSource(strings = ["130", "69"])
    fun `vardokresponse statistical units are values in UnitTypes PERSON`(vardokId: String) {
        val vardokresponse = vardokService.getVardokItem(vardokId)
        val result = vardokresponse?.let { mapVardokStatisticalUnitToUnitTypes(it) }
        assertThat(result).isEqualTo(listOf("20"))
    }
}

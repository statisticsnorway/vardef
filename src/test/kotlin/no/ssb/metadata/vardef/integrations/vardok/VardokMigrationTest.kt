package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.convertions.getValidDates
import no.ssb.metadata.vardef.integrations.vardok.convertions.mapVardokStatisticalUnitToUnitTypes
import no.ssb.metadata.vardef.integrations.vardok.convertions.mapVardokSubjectAreaToSubjectFiled
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL
import java.util.stream.Stream

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
    fun `map vardok missing valid from date but has valid to date`() {
        val varDefInput = vardokService.fetchMultipleVardokItemsByLanguage("134")
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.validFrom).isEqualTo("1900-01-01")
        assertThat(vardokTransform.validUntil).isEqualTo("2005-12-31")
    }

    @Test
    fun `map vardok missing valid end date`() {
        val res = vardokService.getVardokItem("1422")
        val mappedUntilDate = res?.let { getValidDates(it).second }
        assertThat(mappedUntilDate).isNull()
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
            assertThat(mapResult["nb"]).isNotNull()
        }
    }

    @Test
    fun `vardok item has not valid dates`() {
        val result = vardokService.getVardokItem("100")
        assertThat(result?.dc?.valid).isNullOrEmpty()
        val varDefInput = vardokService.fetchMultipleVardokItemsByLanguage("100")
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.validFrom).isEqualTo("1900-01-01")
        assertThat(vardokTransform.validUntil).isNull()
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

    @Test
    fun `vardokresponse subject area are values in SubjectFields`() {
        val vardokresponse = vardokService.getVardokItem("130")
        val result = vardokresponse?.let { mapVardokSubjectAreaToSubjectFiled(it) }
        assertThat(result).isEqualTo(listOf("sk"))
    }

    @Test
    fun `vardokresponse subject area incorrect input`() {
        assertThatThrownBy {
            val vardokresponse = vardokService.getVardokItem("99999")
            vardokresponse?.let { mapVardokSubjectAreaToSubjectFiled(it) }
        }.isInstanceOf(OutdatedSubjectAreaException::class.java)
            .hasMessageContaining("Vardok id 3125 SubjectArea has outdated subject area value and can not be saved")
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.integrations.vardok.VardokResponseTest#mapExternalDocument")
    fun `externalReferenceUri field is set with value from externalDocument`(
        vardokId: String,
        expectedResult: URL?,
    ) {
        val varDefInput = vardokService.fetchMultipleVardokItemsByLanguage(vardokId)
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.externalReferenceUri).isEqualTo(expectedResult)
    }

    @Test
    fun `vardokresponse statistical unit incorrect input`() {
        assertThatThrownBy {
            val vardokresponse = vardokService.getVardokItem("0000")
            vardokresponse?.let { mapVardokStatisticalUnitToUnitTypes(it) }
        }.isInstanceOf(OutdatedUnitTypesException::class.java)
            .hasMessageContaining("Vardok id 0000 StatisticalUnit has outdated unit types and can not be saved")
    }

    @ParameterizedTest
    @MethodSource("mapUnitTypes")
    fun `test unit types special cases`(
        vardokId: String,
        expectedResult: List<String>,
    ) {
        val result = vardokService.fetchMultipleVardokItemsByLanguage(vardokId)
        val varDefInput = VardokService.extractVardefInput(result)
        AssertionsForClassTypes.assertThat(varDefInput.unitTypes).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun mapUnitTypes(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "3125",
                    listOf("21"),
                ),
                arguments(
                    "2141",
                    listOf("04"),
                ),
                arguments(
                    "3246",
                    listOf("12", "13"),
                ),
            )
    }
}

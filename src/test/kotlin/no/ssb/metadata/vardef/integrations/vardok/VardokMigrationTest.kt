package no.ssb.metadata.vardef.integrations.vardok

import com.fasterxml.jackson.databind.JsonMappingException
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import no.ssb.metadata.vardef.integrations.vardok.conversions.StatisticalSubjects
import no.ssb.metadata.vardef.integrations.vardok.conversions.getValidDates
import no.ssb.metadata.vardef.integrations.vardok.conversions.mapVardokStatisticalUnitToUnitTypes
import no.ssb.metadata.vardef.integrations.vardok.conversions.mapVardokSubjectAreaToSubjectField
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.AssertionsForClassTypes
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL
import java.util.stream.Stream

// @MicronautTest
class VardokMigrationTest : BaseVardefTest() {
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
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("134") }
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
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("100") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.validFrom).isEqualTo("1900-01-01")
        assertThat(vardokTransform.validUntil).isNull()
    }

    @Test
    fun `data element name with uppercase`() {
        val vardok = vardokService.getVardokItem("130")
        assertThat(vardok?.variable?.dataElementName).isEqualTo("Ufg")
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("130") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        val afterMigration = JSONObject(vardokTransform)
        assertThat(afterMigration["shortName"]).isEqualTo("ufg")
    }

    @ParameterizedTest
    @ValueSource(strings = ["130", "69", "1416"])
    fun `vardokresponse statistical units are values in UnitTypes PERSON`(vardokId: String) {
        val vardokresponse = vardokService.getVardokItem(vardokId)
        val result = vardokresponse?.let { mapVardokStatisticalUnitToUnitTypes(it) }
        assertThat(result).isEqualTo(listOf("20"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["2413"])
    fun `vardokresponse statistical unit is in language nn`(vardokId: String) {
        val vardokresponse = vardokService.getVardokItem(vardokId)
        val result = vardokresponse?.let { mapVardokStatisticalUnitToUnitTypes(it) }
        assertThat(result).isEqualTo(listOf("13"))
    }

    @Test
    fun `vardokresponse subject area are values in SubjectFields`() {
        val vardokresponse = vardokService.getVardokItem("130")
        val result = vardokresponse?.let { mapVardokSubjectAreaToSubjectField(it) }
        assertThat(result).isEqualTo(listOf(StatisticalSubjects.SOCIAL_CONDITIONS_WELFARE_AND_CRIME))
    }

    @Test
    fun `vardokresponse subject area incorrect input`() {
        assertThat(
            vardokService.getVardokItem("99999")?.let { mapVardokSubjectAreaToSubjectField(it) },
        ).isEqualTo(emptyList<StatisticalSubjects>())
    }

    @ParameterizedTest
    @MethodSource("mapExternalDocument")
    fun `externalReferenceUri field is set with value from externalDocument`(
        vardokId: String,
        expectedResult: URL?,
    ) {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage(vardokId) }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.externalReferenceUri).isEqualTo(expectedResult)
    }

    @Test
    fun `map ConceptVariableRelations none`() {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("948") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.relatedVariableDefinitionUris).isEmpty()
    }

    @Test
    fun `map ConceptVariableRelations several`() {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("2") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.relatedVariableDefinitionUris?.size).isEqualTo(5)
        assertThat(vardokTransform.relatedVariableDefinitionUris?.last())
            .isEqualTo("http://www.ssb.no/conceptvariable/vardok/11")
        assertThat(vardokTransform.relatedVariableDefinitionUris is List<String>)
    }

    @Test
    fun `map single ConceptVariableRelation`() {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("1245") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.relatedVariableDefinitionUris?.first()).isEqualTo(
            "http://www.ssb.no/conceptvariable/vardok/1246",
        )
    }

    @Test
    fun `vardokresponse statistical unit incorrect input`() {
        assertThatThrownBy {
            val vardokresponse = vardokService.getVardokItem("0000")
            vardokresponse?.let { mapVardokStatisticalUnitToUnitTypes(it) }
        }.isInstanceOf(StatisticalUnitException::class.java)
            .hasMessageContaining("Vardok ID 0000: StatisticalUnit is either missing or contains outdated unit types.")
    }

    @Test
    fun `Vardok not found`() {
        assertThatThrownBy {
            vardokService.getVardokItem("21")
        }.isInstanceOf(VardokNotFoundException::class.java)
            .hasMessageContaining("Vardok id 21 not found")
    }

    @Test
    fun `Vardok not found by language`() {
        assertThatThrownBy {
            vardokService.getVardokByIdAndLanguage("0002", "en")
        }.isInstanceOf(VardokNotFoundException::class.java)
            .hasMessageContaining("Id 0002 in language: en not found")
    }

    @Test
    fun `Vardokresponse invalid characters`() {
        assertThatThrownBy {
            vardokService.getVardokItem("0001")
        }.isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("Unexpected character")
    }

    @Test
    fun `Vardokresponse invalid characters by language`() {
        assertThatThrownBy {
            vardokService.getVardokByIdAndLanguage("0001", "en")
        }.isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("Unexpected character")
    }

    @Test
    fun `Vardokresponse missing fields`() {
        assertThatThrownBy {
            vardokService.getVardokItem("0002")
        }.isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("Cannot construct instance of `no.ssb.metadata.vardef.integrations.vardok.models.Variable`")
    }

    @Test
    fun `Vardokresponse creative dates`() {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("0003") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.validFrom).isEqualTo("10039081")

        val varDefInput2 = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("0004") }
        val vardokTransform2 = VardokService.extractVardefInput(varDefInput2)
        assertThat(vardokTransform2.validFrom).isEqualTo("1003-90-81")
    }

    @ParameterizedTest
    @MethodSource("mapUnitTypes")
    fun `test unit types special cases`(
        vardokId: String,
        expectedResult: List<String>,
    ) {
        val result = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage(vardokId) }
        val varDefInput = VardokService.extractVardefInput(result)
        AssertionsForClassTypes.assertThat(varDefInput.unitTypes).isEqualTo(expectedResult)
    }

    @Test
    fun `duplicate short name`() {
        assertThat(runBlocking { vardokService.isDuplicate("bus") }).isTrue()
        assertThat(runBlocking { vardokService.isDuplicate("non_existing_name") }).isFalse()
    }

    @Test
    fun `set generated short name if duplicate short name exists`() {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("0005") }
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.shortName).contains("generert")
    }

    @Test
    fun `new norwegian is primary language`() {
        val varDefInput = runBlocking { vardokService.fetchMultipleVardokItemsByLanguage("2413") }
        assertThat(varDefInput["nn"]?.common?.title).isEqualTo("Sum utgifter")
        assertThat(varDefInput["nb"]?.common?.title).isNull()
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
                arguments(
                    "590",
                    listOf("12", "13", "20"),
                ),
            )

        @JvmStatic
        fun mapExternalDocument(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Vardok id 2 has external document",
                    "2",
                    "http://www.ssb.no/emner/05/90/notat_200372/notat_200372.pdf",
                ),
                argumentSet(
                    "Vardok id 130 has not external document",
                    "130",
                    null,
                ),
                argumentSet(
                    "Vardok id 123 has external document",
                    "123",
                    "http://www.ssb.no/emner/02/01/10/innvbef/om.html",
                ),
                argumentSet(
                    "Vardok id 1245 has invalid external document",
                    "1245",
                    null,
                ),
            )
    }
}

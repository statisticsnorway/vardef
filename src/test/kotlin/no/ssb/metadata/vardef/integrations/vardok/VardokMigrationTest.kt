package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.*
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
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
            assertThat(mapResult["nb"]).isNotNull()
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

    @ParameterizedTest
    @MethodSource("mapCommentField")
    fun `map Vardok notes and calculation to vardef comment`(
        vardokId: String,
        expectedCommentNB: String?,
        expectedCommentNN: String?,
        expectedCommentEN: String?,
        isConcatenated: Boolean,
    ) {
        val vardok = vardokService.getVardokItem(vardokId)
        val notes = vardok?.common?.notes
        val calculation = vardok?.variable?.calculation
        val varDefInput = vardokService.fetchMultipleVardokItemsByLanguage(vardokId)
        val vardokTransform = VardokService.extractVardefInput(varDefInput)
        assertThat(vardokTransform.comment?.nb).isEqualTo(expectedCommentNB)
        assertThat(vardokTransform.comment?.nn).isEqualTo(expectedCommentNN)
        assertThat(vardokTransform.comment?.en).isEqualTo(expectedCommentEN)
        if (isConcatenated) {
            assertThat(vardokTransform.comment?.nb).containsSubsequence(notes, calculation)
        }
    }

    companion object {
        @JvmStatic
        fun mapCommentField(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "comment is null when notes and calculation are null",
                    "2",
                    null,
                    null,
                    null,
                    false,
                ),
                argumentSet(
                    "comment is notes when notes is not null and calculation is null",
                    "901",
                    "Opplysningene er hentet fra boligskjemaet i FoB2001, spørsmål 21.",
                    null,
                    "The information is collected from the Housing form in Census 2001, question 21.",
                    false,
                ),
                argumentSet(
                    "comment is calculation when calculation is not null and notes is null",
                    "267",
                    "= P8005 + P8006",
                    null,
                    null,
                    false,
                ),
                argumentSet(
                    "comment has nn language",
                    "1849",
                    "Byggekostnadsindeks for boliger er en veid indeks av byggekostnadsindeks for enebolig av tre og " +
                        "byggekostnadsindeks for boligblokk.",
                    "Byggjekostnadsindeks for bustader i alt er ein vege indeks av einebustader av tre " +
                        "og bustadblokker.",
                    null,
                    false,
                ),
                argumentSet(
                    "comment concatenates calculation and notes when both have values",
                    "1299",
                    "Denne variabelen benyttes både for foretak og bedrift.Beregnes via Næringsoppgaven: " +
                        "Post 9000/9900-post3400-post 3800/3895",
                    null,
                    null,
                    true,
                ),
            )
    }
}

package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.context.annotation.Requires
import io.micronaut.http.exceptions.HttpStatusException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Requires(env = ["integration-test"])
@MicronautTest
class VarDokMigrationTest {
    @Inject
    lateinit var varDokApiService: VarDokApiService

    @Test
    fun `get vardok by id`() {
        val result = varDokApiService.getVarDokItem("901")
        assertThat(result).isNotNull()
        assertThat(result?.dc?.contributor).isEqualTo("Seksjon for befolkningsstatistikk")
        assertThat(result?.common?.title).isEqualTo("Oppvarming, har lukket ovn for fast brensel")
        assertThat(result?.otherLanguages).isEqualTo("en")
        assertThat(result?.type).isEqualTo("ConceptVariable")
        assertThat(result?.xmlLang).isEqualTo("nb")
    }

    @Test
    fun `get vardok by id and language if other languages`() {
        val res = varDokApiService.getVarDokItem("901")
        var englishRes: VardokResponse? = null
        if (res?.otherLanguages != "") {
            englishRes = res?.let { varDokApiService.getVardokByIdAndLanguage("901", it.otherLanguages) }
        }
        assertThat(englishRes?.common?.title).isEqualTo("System for heating, has closed stoves for solid fuel")
        assertThat(englishRes?.id).isEqualTo(res?.id)
    }

    @Test
    fun `map vardok date from`() {
        val res = varDokApiService.getVarDokItem("901")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(10)
        val mappedFromDate = res?.let { mapValidDateFrom(it) }
        assertThat(mappedFromDate).isNotNull()
        assertThat(mappedFromDate).isEqualTo("2001-01-01")
    }

    @Test
    fun `map vardok date until`() {
        val res = varDokApiService.getVarDokItem("901")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(20)
        val mappedUntilDate = res?.let { mapValidDateUntil(it) }
        assertThat(mappedUntilDate).isNotNull()
        assertThat(mappedUntilDate).isEqualTo("2001-12-31")
    }

    @Test
    fun `map vardok missing valid date`() {
        val res = varDokApiService.getVarDokItem("100")
        assertThat(res).isNotNull()
        val mappedFromDate = res?.let { mapValidDateFrom(it) }
        assertThat(mappedFromDate).isNull()
    }

    @Test
    fun `map vardok missing valid end date`() {
        val res = varDokApiService.getVarDokItem("1422")
        val mappedUntilDate = res?.let { mapValidDateUntil(it) }
        assertThat(mappedUntilDate).isNull()
    }

    @ParameterizedTest
    @ValueSource(strings = ["1422", "1919", "2", "5", "123"])
    fun `set link to vardok`(vardokId: String) {
        val result = varDokApiService.getVarDokItem(vardokId)
        if (result != null) {
            val mapResult: MutableMap<String, VardokResponse> = mutableMapOf("nb" to result)
            val renderVarDok = toVarDefFromVarDok(mapResult)
            assertThat(renderVarDok).isNotNull
            assertThat(
                renderVarDok.externalReferenceUri.toString(),
            ).isEqualTo("https://www.ssb.no/a/xml/metadata/conceptvariable/vardok/$vardokId")
        }
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 18, 20, 25, 26, 27, 28, 30, 33, 42, 49, 50, 51, 52, 69, 76,
            88, 89, 90, 91, 117, 118, 119, 120,
        ],
    )
    fun `map owner from vardok`(vardokId: Int) {
        val result = varDokApiService.getVarDokItem(vardokId.toString())
        assertThat(result).isNotNull
        assertThat(result?.common?.contactDivision).isNotNull
        assertThat(result?.common?.contactDivision?.codeValue).isNotNull()
        assertThat(result?.common?.contactDivision?.codeText).isNotNull()
    }

    @Test
    fun `vardok id not found`() {
        val exception: Exception =
            org.junit.jupiter.api.Assertions.assertThrows(HttpStatusException::class.java) {
                varDokApiService.getVarDokItem("1")
            }
        assertThat(exception).isInstanceOf(HttpStatusException::class.java)
        val expectedMessage = "Id not found"
        val actualMessage = exception.message

        assertThat(expectedMessage).isEqualTo(actualMessage)
    }

    @Test
    fun `vardok item has not short name`() {
        val result = varDokApiService.getVarDokItem("2450")
        if (result != null) {
            val mapResult: MutableMap<String, VardokResponse> = mutableMapOf("nb" to result)
            val exception: VardokException =
                org.junit.jupiter.api.Assertions.assertThrows(VardokException::class.java) {
                    varDokApiService.createVarDefInputFromVarDokItems(mapResult)
                }
            assertThat(exception).isInstanceOf(VardokException::class.java)
            val expectedMessage = "Vardok is missing short name and can not be saved"
            val actualMessage = exception.message

            assertThat(expectedMessage).isEqualTo(actualMessage)
        }
    }

    @Test
    fun `vardok item has not valid dates`() {
        val result = varDokApiService.getVarDokItem("100")
        if (result != null) {
            val mapResult: MutableMap<String, VardokResponse> = mutableMapOf("nb" to result)
            val exception: MissingValidDatesException =
                org.junit.jupiter.api.Assertions.assertThrows(MissingValidDatesException::class.java) {
                    varDokApiService.createVarDefInputFromVarDokItems(mapResult)
                }
            assertThat(exception).isInstanceOf(MissingValidDatesException::class.java)
            val expectedMessage = "Vardok is missing valid dates and can not be saved"
            val actualMessage = exception.message

            assertThat(expectedMessage).isEqualTo(actualMessage)
        }
    }
}

package no.ssb.metadata.integrations.vardok

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.lang.Exception

@MicronautTest
//@Requires(env = ["integration-test"])
class VarDokMigrationTest {
    @Inject
    lateinit var varDokApiService: VarDokApiService

    @Test
    fun `get vardok by id`() {
        val result = varDokApiService.getVarDokItem("1422")
        assertThat(result).isNotNull()
        assertThat(result?.dc?.contributor).isEqualTo("Seksjon for regnskapsstatistikk")
        assertThat(result?.dc?.title).isEqualTo("Aksje")
        assertThat(result?.common?.title).isEqualTo("Aksje")
        assertThat(result?.otherLanguages).isEqualTo("en")
        assertThat(result?.type).isEqualTo("ConceptVariable")
        assertThat(result?.xmlLang).isEqualTo("nb")
    }

    @Test
    fun `get list of vardok results by id`() {
        val idList = listOf("1422", "1919")
        val result = varDokApiService.getListOfVardokById(idList)
        assertThat(result).isNotNull()
        result.forEach { assertThat(it?.id).isNotNull() }
        assertThat(result[0]?.dc?.contributor).isEqualTo("Seksjon for regnskapsstatistikk")
        assertThat(result).size().isEqualTo(idList.size)
    }

    @Test
    fun `get vardok by id and language if other languages`() {
        val res = varDokApiService.getVarDokItem("1422")
        var englishRes: FIMD? = null
        if (res?.otherLanguages != "") {
            englishRes = res?.let { varDokApiService.getVardokByIdAndLanguage("1422", it.otherLanguages) }
        }
        assertThat(englishRes?.common?.title).isEqualTo("Share")
        assertThat(englishRes?.id).isEqualTo(res?.id)
    }

    @Test
    fun `map vardok date from`() {
        val res = varDokApiService.getVarDokItem("1422")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(10)
        val mappedFromDate = res?.let { mapValidDateFrom(it) }
        assertThat(mappedFromDate).isNotNull()
        assertThat(mappedFromDate).isEqualTo("1984-01-01")
    }

    @Test
    fun `map vardok date until`() {
        val res = varDokApiService.getVarDokItem("123")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(20)
        val mappedUntilDate = res?.let { mapValidDateUntil(it) }
        assertThat(mappedUntilDate).isNotNull()
        assertThat(mappedUntilDate).isEqualTo("2002-12-31")
    }

    @Test
    fun `map vardok missing valid date`() {
        val res = varDokApiService.getVarDokItem("100")
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
            val mapResult: MutableMap<String, FIMD> = mutableMapOf("nb" to result)
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
    fun `catch vardok id not found`() {
        val result = varDokApiService.getVarDokItem("1")
        assertThat(result).isNull()
    }
}

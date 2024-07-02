package no.ssb.metadata.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
class VarDokMigrationTest {
    @Inject
    lateinit var varDokApiService: VarDokApiService

    @Test
    fun `Test migration`() {
        val result = varDokApiService.getVarDokResponse()
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("urn:ssb:conceptvariable:vardok:100")
        assertThat(result?.dc?.contributor).isNotNull()
        assertThat(result?.dc?.contributor).isEqualTo("Seksjon for befolkningsstatistikk")
        assertThat(result?.common?.title).isEqualTo("Adressenavn")
    }

    @Test
    fun `Get vardok by id`() {
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
    fun `transform vardok to vardef`() {
        val result = varDokApiService.getVarDokItem("1422")
        val renderVarDok = result?.let { migrateVarDok(it) }
        assertThat(renderVarDok?.name?.nb).isEqualTo(result?.dc?.title)
        assertThat(renderVarDok?.definition?.nb).isEqualTo(result?.common?.description)
        assertThat(renderVarDok?.validFrom).isEqualTo("1984-01-01")
    }

    @Test
    fun `get list of vardok results by id`() {
        val idList = listOf("100", "1422", "2001", "1919")
        val result = varDokApiService.getListOfVardokById(idList)
        assertThat(result).isNotNull()
        result.forEach { assertThat(it?.id).isNotNull() }
        assertThat(result[0]?.dc?.contributor).isEqualTo("Seksjon for befolkningsstatistikk")
        assertThat(result).size().isEqualTo(idList.size)
    }

    @Test
    fun `iterate list`() {
        val resList: ArrayList<FIMD> = arrayListOf()
        var res: FIMD?
        var counter = 0
        val invalidList: ArrayList<Int> = arrayListOf()
        for (i in 1..100) {
            res = varDokApiService.getVarDokItem("$i")
            if (res != null) {
                resList.add(res)
                println("Id found $i")
            } else {
                counter += 1
                println("Not valid id $counter")
                invalidList.add(i)
            }
        }
        assertThat(resList).isNotNull()
        assertThat(resList).size().isEqualTo(100 - counter)
        assertThat(resList[0].dc).isNotNull()
        println(invalidList)
    }

    @Test
    fun `Get vardok by id and language if other languages`() {
        val res = varDokApiService.getVarDokItem("1422")
        var englishRes: FIMD? = null
        if (res?.otherLanguages != "") {
            englishRes = res?.let { varDokApiService.getVardokByIdAndLanguage("1422", it.otherLanguages) }
        }
        assertThat(englishRes?.common?.title).isEqualTo("Share")
        assertThat(englishRes?.id).isEqualTo(res?.id)
    }

    @Test
    fun `Map vardok date from`() {
        val res = varDokApiService.getVarDokItem("1422")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(10)
        val mappedFromDate = res?.let { mapValidDateFrom(it) }
        assertThat(mappedFromDate).isNotNull()
        assertThat(mappedFromDate).isEqualTo("1984-01-01")
    }

    @Test
    fun `Map vardok date until`() {
        val res = varDokApiService.getVarDokItem("123")
        assertThat(res?.dc?.valid).isNotNull()
        assertThat(res?.dc?.valid).hasSizeGreaterThan(20)
        val mappedUntilDate = res?.let { mapValidDateUntil(it) }
        assertThat(mappedUntilDate).isNotNull()
        assertThat(mappedUntilDate).isEqualTo("2002-12-31")
    }

    @Test
    fun `Map vardok missing valid date`() {
        val res = varDokApiService.getVarDokItem("100")
        val mappedFromDate = res?.let { mapValidDateFrom(it) }
        assertThat(mappedFromDate).isNull()
    }

    @Test
    fun `Map vardok missing valid end date`() {
        val res = varDokApiService.getVarDokItem("1422")
        val mappedUntilDate = res?.let { mapValidDateUntil(it) }
        assertThat(mappedUntilDate).isNull()
    }
}

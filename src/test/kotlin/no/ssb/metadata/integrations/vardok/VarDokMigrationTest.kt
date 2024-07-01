package no.ssb.metadata.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.VarDokApiService
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
    fun `get list of vardok results by id`() {
        val idList = listOf("100", "1422", "2001","1919")
        val result = varDokApiService.getListOfVardokById(idList)
        assertThat(result).isNotNull()
        result.forEach { assertThat(it?.id).isNotNull() }
        assertThat(result[0]?.dc?.contributor).isEqualTo("Seksjon for befolkningsstatistikk")
        assertThat(result).size().isEqualTo(idList.size)
    }
}

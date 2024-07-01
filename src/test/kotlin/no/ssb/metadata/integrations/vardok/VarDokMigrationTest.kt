package no.ssb.metadata.integrations.vardok

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.VardokApiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
class VarDokMigrationTest {

    @Inject
    lateinit var varDokService: VardokApiService

    @Test
    fun `Test migration`() {
        val result = varDokService.getVardokResponse()
        assertThat(result).isNotNull()
    }
}
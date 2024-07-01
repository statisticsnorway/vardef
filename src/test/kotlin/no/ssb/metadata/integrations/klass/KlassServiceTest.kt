package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = true)
class KlassServiceTest() {
    @Inject
    lateinit var klassService: KlassService

    @Test
    fun `get klass code item test`() {
        klassService.getCodeItemFor("702", "01", SupportedLanguages.NB)?.let { klassItem ->
            assertThat(klassItem.referenceUri).isEqualTo("https://data.ssb.no/api/klass/v1/classifications/702/")
            assertThat(klassItem.code).isEqualTo("01")
            assertThat(klassItem.title).isEqualTo("Adresse")
        }

        klassService.getCodeItemFor("702", "41", SupportedLanguages.NB)?.let { klassItem ->
            assertThat(klassItem).isEqualTo(null)
        }

        klassService.getCodeItemFor("702", "01", SupportedLanguages.NN)?.let { klassItem ->
            assertThat(klassItem.referenceUri).isEqualTo("https://data.ssb.no/api/klass/v1/classifications/702/")
            assertThat(klassItem.code).isEqualTo("01")
            assertThat(klassItem.title).isEqualTo(null)
        }
    }
}
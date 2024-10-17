package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.SupportedLanguages
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = true)
class KlassServiceTest {
    @Inject
    lateinit var klassService: KlassService

    @Test
    fun `get klass code item for unit types test`() {
        klassService
            .getCodeItemFor("702", "01", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/702")
                assertThat(klassItem.code).isEqualTo("01")
                assertThat(klassItem.title).isEqualTo("Adresse")
            }

        klassService
            .getCodeItemFor("702", "17", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo("17")
                assertThat(klassItem.title).isEqualTo("Kommune (geografisk)")
            }

        klassService
            .getCodeItemFor("702", "41", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem).isEqualTo(null)
            }

        klassService
            .getCodeItemFor("702", "01", SupportedLanguages.NN)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/702")
                assertThat(klassItem.code).isEqualTo("01")
                assertThat(klassItem.title).isEqualTo(null)
            }
    }

    @Test
    fun `get klass code item for subject fields test`() {
        klassService
            .getCodeItemFor("618", "vf", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/618")
                assertThat(klassItem.code).isEqualTo("vf")
                assertThat(klassItem.title).isEqualTo("Bedrifter, foretak og regnskap")
            }

        klassService
            .getCodeItemFor("618", "al", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo("al")
                assertThat(klassItem.title).isEqualTo("Arbeid og lønn")
            }

        klassService
            .getCodeItemFor("618", "vgh", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem).isEqualTo(null)
            }
    }

    @Test
    fun `get klass code item for measurement type test`() {
        klassService
            .getCodeItemFor("303", "02", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/303")
                assertThat(klassItem.code).isEqualTo("02")
                assertThat(klassItem.title).isEqualTo("Antall")
            }

        klassService
            .getCodeItemFor("303", "17.23", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo("17.23")
                assertThat(klassItem.title).isEqualTo("øre per kWh")
            }

        klassService
            .getCodeItemFor("303", "156.3", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo(null)
            }
    }
}

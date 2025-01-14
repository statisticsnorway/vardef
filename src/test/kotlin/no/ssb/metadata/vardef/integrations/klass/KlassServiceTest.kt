package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.MEASUREMENT_TYPE_KLASS_CODE
import no.ssb.metadata.vardef.constants.SUBJECT_FIELDS_KLASS_CODE
import no.ssb.metadata.vardef.constants.UNIT_TYPES_KLASS_CODE
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
            .renderCode(UNIT_TYPES_KLASS_CODE, "01", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/$UNIT_TYPES_KLASS_CODE")
                assertThat(klassItem.code).isEqualTo("01")
                assertThat(klassItem.title).isEqualTo("Adresse")
            }

        klassService
            .renderCode(UNIT_TYPES_KLASS_CODE, "17", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo("17")
                assertThat(klassItem.title).isEqualTo("Kommune (geografisk)")
            }

        klassService
            .renderCode(UNIT_TYPES_KLASS_CODE, "17", SupportedLanguages.EN)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/en/klass/klassifikasjoner/$UNIT_TYPES_KLASS_CODE")
                assertThat(klassItem.code).isEqualTo("17")
                assertThat(klassItem.title).isEqualTo(null)
            }

        klassService
            .renderCode(UNIT_TYPES_KLASS_CODE, "41", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem).isEqualTo(null)
            }

        klassService
            .renderCode(UNIT_TYPES_KLASS_CODE, "01", SupportedLanguages.NN)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/$UNIT_TYPES_KLASS_CODE")
                assertThat(klassItem.code).isEqualTo("01")
                assertThat(klassItem.title).isEqualTo(null)
            }
    }

    @Test
    fun `get klass code item for subject fields test`() {
        klassService
            .renderCode(SUBJECT_FIELDS_KLASS_CODE, "vf", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/$SUBJECT_FIELDS_KLASS_CODE")
                assertThat(klassItem.code).isEqualTo("vf")
                assertThat(klassItem.title).isEqualTo("Bedrifter, foretak og regnskap")
            }

        klassService
            .renderCode(SUBJECT_FIELDS_KLASS_CODE, "al", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo("al")
                assertThat(klassItem.title).isEqualTo("Arbeid og lønn")
            }

        klassService
            .renderCode(SUBJECT_FIELDS_KLASS_CODE, "vgh", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem).isEqualTo(null)
            }
    }

    @Test
    fun `get klass code item for measurement type test`() {
        klassService
            .renderCode(MEASUREMENT_TYPE_KLASS_CODE, "02", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("https://www.ssb.no/klass/klassifikasjoner/$MEASUREMENT_TYPE_KLASS_CODE")
                assertThat(klassItem.code).isEqualTo("02")
                assertThat(klassItem.title).isEqualTo("Antall")
            }

        klassService
            .renderCode(MEASUREMENT_TYPE_KLASS_CODE, "17.23", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo("17.23")
                assertThat(klassItem.title).isEqualTo("øre per kWh")
            }

        klassService
            .renderCode(MEASUREMENT_TYPE_KLASS_CODE, "156.3", SupportedLanguages.NB)
            ?.let { klassItem ->
                assertThat(klassItem.code).isEqualTo(null)
            }
    }
}

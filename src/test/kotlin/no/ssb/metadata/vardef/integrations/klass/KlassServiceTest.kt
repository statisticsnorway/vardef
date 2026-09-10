package no.ssb.metadata.vardef.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.KLASS_ID_MEASUREMENT_TYPE
import no.ssb.metadata.vardef.constants.KLASS_ID_SUBJECT_FIELDS
import no.ssb.metadata.vardef.constants.KLASS_ID_UNIT_TYPES
import no.ssb.metadata.vardef.constants.KLASS_URL_WEB_BASE_DEFAULT
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.KlassReference
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
            .renderCode(KLASS_ID_UNIT_TYPES, "01", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_UNIT_TYPES")
                assertThat(klassItem.code).isEqualTo("01")
                assertThat(klassItem.title).isEqualTo("Adresse")
            }

        klassService
            .renderCode(KLASS_ID_UNIT_TYPES, "17", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.code).isEqualTo("17")
                assertThat(klassItem.title).isEqualTo("Kommune (geografisk)")
            }

        klassService
            .renderCode(KLASS_ID_UNIT_TYPES, "17", SupportedLanguages.EN)
            .let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_UNIT_TYPES")
                assertThat(klassItem.code).isEqualTo("17")
                assertThat(klassItem.title).isEqualTo(null)
            }

        klassService
            .renderCode(KLASS_ID_UNIT_TYPES, "41", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem).isEqualTo(
                    KlassReference(
                        referenceUri = "$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_UNIT_TYPES",
                        code = "41",
                        title = null,
                    ),
                )
            }

        klassService
            .renderCode(KLASS_ID_UNIT_TYPES, "01", SupportedLanguages.NN)
            .let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_UNIT_TYPES")
                assertThat(klassItem.code).isEqualTo("01")
                assertThat(klassItem.title).isEqualTo(null)
            }
    }

    @Test
    fun `get klass code item for subject fields test`() {
        klassService
            .renderCode(KLASS_ID_SUBJECT_FIELDS, "vf", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_SUBJECT_FIELDS")
                assertThat(klassItem.code).isEqualTo("vf")
                assertThat(klassItem.title).isEqualTo("Bedrifter, foretak og regnskap")
            }

        klassService
            .renderCode(KLASS_ID_SUBJECT_FIELDS, "al", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.code).isEqualTo("al")
                assertThat(klassItem.title).isEqualTo("Arbeid og lønn")
            }

        klassService
            .renderCode(KLASS_ID_SUBJECT_FIELDS, "vgh", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem).isEqualTo(
                    KlassReference(
                        referenceUri = "$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_SUBJECT_FIELDS",
                        code = "vgh",
                        title = null,
                    ),
                )
            }
    }

    @Test
    fun `get klass code item for measurement type test`() {
        klassService
            .renderCode(KLASS_ID_MEASUREMENT_TYPE, "02", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.referenceUri).isEqualTo("$KLASS_URL_WEB_BASE_DEFAULT/$KLASS_ID_MEASUREMENT_TYPE")
                assertThat(klassItem.code).isEqualTo("02")
                assertThat(klassItem.title).isEqualTo("Antall")
            }

        klassService
            .renderCode(KLASS_ID_MEASUREMENT_TYPE, "17", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.code).isEqualTo("17")
                assertThat(klassItem.title).isEqualTo("Sammensatte måleenheter")
            }

        klassService
            .renderCode(KLASS_ID_MEASUREMENT_TYPE, "156.3", SupportedLanguages.NB)
            .let { klassItem ->
                assertThat(klassItem.code).isEqualTo("156.3")
            }
    }
}

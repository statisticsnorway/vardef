package no.ssb.metadata.integrations.klass

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.validation.validator.Validator
import jakarta.inject.Inject
import no.ssb.metadata.models.KlassReference
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = true)
class KlassServiceTest(private val validator: Validator) {
    @Inject
    lateinit var klassService: KlassService

    @Test
    fun `get klass code item test`() {
        klassService.getCodeItemFor("702", "01", SupportedLanguages.NB)?.let { klassItem ->
            assertThat(klassItem.code).isEqualTo("01")
            assertThat(klassItem.title).isEqualTo("Adresse")
        }

        klassService.getCodeItemFor("702", "41", SupportedLanguages.NB)?.let { klassItem ->
            assertThat(klassItem).isEqualTo(null)
        }

        klassService.getCodeItemFor("702", "01", SupportedLanguages.NN)?.let { klassItem ->
            assertThat(klassItem.code).isEqualTo("01")
            assertThat(klassItem.title).isEqualTo(null)
        }
    }
}
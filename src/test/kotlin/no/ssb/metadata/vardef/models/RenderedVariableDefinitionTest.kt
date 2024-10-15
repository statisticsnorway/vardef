package no.ssb.metadata.vardef.models

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RenderedVariableDefinitionTest : BaseVardefTest() {
    @Inject
    lateinit var klassService: KlassApiService

    @Test
    fun `unit type code 20 title is person`()  {
        val savedVariableDefinitionRendered =
            INCOME_TAX_VP1_P1.copy(
                unitTypes = listOf("20"),
            ).render(SupportedLanguages.NB, klassService)
        assertThat(savedVariableDefinitionRendered.unitTypes[0]?.title).isEqualToIgnoringCase("person")
    }

    @Test
    fun `unit type code 20 title is person regardless of date`()  {
        val dates = listOf<LocalDate>(
            LocalDate.of(1970, 1, 1),
            LocalDate.of(1980, 12, 31),
            LocalDate.of(2015, 1, 31),
            LocalDate.of(2019, 5, 8),
            LocalDate.of(2024, 11, 25),
            LocalDate.of(2060, 3, 2),)
        for (date in dates) {
            val savedVariableDefinitionRendered =
                INCOME_TAX_VP1_P1.copy(
                    unitTypes = listOf("20"),
                    validFrom = date
                ).render(SupportedLanguages.NB, klassService)
            assertThat(savedVariableDefinitionRendered.unitTypes[0]?.title).isEqualToIgnoringCase("person")
        }

    }
}

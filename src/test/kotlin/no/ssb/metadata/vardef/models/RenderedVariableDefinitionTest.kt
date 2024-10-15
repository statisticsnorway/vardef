package no.ssb.metadata.vardef.models

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RenderedVariableDefinitionTest: BaseVardefTest() {

    @Inject
    lateinit var klassService: KlassApiService

    @Test
    fun `unit type code 20 title is person`(){
        val savedVariableDefinitionRendered = INCOME_TAX_VP1_P1.copy(
            unitTypes = listOf("20")
        ).render(SupportedLanguages.NB, klassService)
        assertThat(savedVariableDefinitionRendered.unitTypes[0]?.title).isEqualToIgnoringCase("person")
    }
}
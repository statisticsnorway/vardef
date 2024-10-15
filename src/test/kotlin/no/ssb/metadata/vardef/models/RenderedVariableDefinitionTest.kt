package no.ssb.metadata.vardef.models

import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.klass.service.KlassApiService
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

class RenderedVariableDefinitionTest : BaseVardefTest() {
    @Inject
    lateinit var klassService: KlassService

    //@Inject
    //lateinit var klassService: KlassApiService

    private val dates = listOf<LocalDate>(
        LocalDate.of(1910, 6, 13),
        LocalDate.of(1970, 1, 1),
        LocalDate.of(1980, 12, 31),
        LocalDate.of(2015, 1, 31),
        LocalDate.of(2019, 5, 8),
        LocalDate.of(2024, 11, 25),
        LocalDate.of(2060, 3, 2),)

    companion object {
        @JvmStatic
        fun unitTypes(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "07",
                    "Fylke (forvaltning)"
                ),
                arguments(
                    "13",
                    "Bedrift"
                ),
                arguments(
                    "25",
                    "Transaksjon"
                ),
                arguments(
                    "28",
                    "Verdipapir"
                ),
                arguments(
                    "12",
                    "Foretak"
                ),
                arguments(
                    "20",
                    "person"
                ),
            )

        @JvmStatic
        fun subjectFields(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "al04",
                    "Arbeidsmiljø, sykefravær og arbeidskonflikter"
                ),
                arguments(
                    "al05",
                    "Lønn og arbeidskraftkostnader"
                ),
                arguments(
                    "vf",
                    "Bedrifter, foretak og regnskap"
                ),
                arguments(
                    "vf05",
                    "Bedrifter og foretak"
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("unitTypes")
    fun `unit type renders the same title regardless of date`(code: String,title: String)  {
        for (date in dates) {
            val savedVariableDefinitionRendered =
                INCOME_TAX_VP1_P1.copy(
                    unitTypes = listOf(code),
                    validFrom = date
                ).render(SupportedLanguages.NB, klassService)
            assertThat(savedVariableDefinitionRendered.unitTypes[0]?.title).isEqualToIgnoringCase(title)
        }

    }

    @ParameterizedTest
    @MethodSource("subjectFields")
    fun `subject fields renders the same title regardless of date`(code: String, title: String) {
        for (date in dates) {
            val savedVariableDefinitionRendered =
                INCOME_TAX_VP1_P1.copy(
                    subjectFields = listOf(code),
                    validFrom = date
                ).render(SupportedLanguages.NB, klassService)
            assertThat(savedVariableDefinitionRendered.subjectFields[0]?.title).isEqualToIgnoringCase(title)

        }
    }
}

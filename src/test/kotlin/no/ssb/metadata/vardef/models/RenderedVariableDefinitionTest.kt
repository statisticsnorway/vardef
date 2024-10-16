package no.ssb.metadata.vardef.models

import jakarta.inject.Inject
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

    private val dates =
        listOf<LocalDate>(
            LocalDate.of(1910, 6, 13),
            LocalDate.of(1970, 1, 1),
            LocalDate.of(1980, 12, 31),
            LocalDate.of(2015, 1, 31),
            LocalDate.of(2019, 5, 8),
            LocalDate.of(2024, 11, 25),
            LocalDate.of(2060, 3, 2),
        )

    companion object {
        @JvmStatic
        fun unitTypes(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "07",
                    "Fylke (forvaltning)",
                ),
                arguments(
                    "13",
                    "Bedrift",
                ),
                arguments(
                    "25",
                    "Transaksjon",
                ),
                arguments(
                    "28",
                    "Verdipapir",
                ),
                arguments(
                    "12",
                    "Foretak",
                ),
                arguments(
                    "20",
                    "Person",
                ),
            )

        @JvmStatic
        fun subjectFields(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "al04",
                    "Arbeidsmiljø, sykefravær og arbeidskonflikter",
                ),
                arguments(
                    "al05",
                    "Lønn og arbeidskraftkostnader",
                ),
                arguments(
                    "vf",
                    "Bedrifter, foretak og regnskap",
                ),
                arguments(
                    "vf05",
                    "Bedrifter og foretak",
                ),
            )

        @JvmStatic
        fun measurementTypes(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "10.01",
                    "Celsius",
                ),
                arguments(
                    "10.4",
                    null,
                ),
                arguments(
                    "11.03",
                    "Implisitt utslippsfaktor",
                ),
                arguments(
                    "12.04",
                    "euro",
                ),
                arguments(
                    "06",
                    "Spenning",
                ),
                arguments(
                    "13",
                    "Masse (vekt)",
                ),
                arguments(
                    "17.01",
                    "antall per km²",
                ),
                arguments(
                    "17.10",
                    "kr per m³",
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("unitTypes")
    fun `unit type renders the same title regardless of date`(
        code: String,
        title: String,
    ) {
        for (date in dates) {
            val savedVariableDefinitionRendered =
                INCOME_TAX_VP1_P1.copy(
                    unitTypes = listOf(code),
                    validFrom = date,
                ).render(SupportedLanguages.NB, klassService)
            assertThat(savedVariableDefinitionRendered.unitTypes[0]?.title).isEqualTo(title)
        }
    }

    @ParameterizedTest
    @MethodSource("subjectFields")
    fun `subject fields renders the same title regardless of date`(
        code: String,
        title: String,
    ) {
        for (date in dates) {
            val savedVariableDefinitionRendered =
                INCOME_TAX_VP1_P1.copy(
                    subjectFields = listOf(code),
                    validFrom = date,
                ).render(SupportedLanguages.NB, klassService)
            assertThat(savedVariableDefinitionRendered.subjectFields[0]?.title).isEqualTo(title)
        }
    }

    @ParameterizedTest
    @MethodSource("measurementTypes")
    fun `measurement type field renders the same title regardless of date`(
        code: String,
        title: String?,
    ) {
        for (date in dates) {
            val savedVariableDefinitionRendered =
                INCOME_TAX_VP1_P1.copy(
                    measurementType = code,
                    validFrom = date,
                ).render(SupportedLanguages.NB, klassService)
            assertThat(savedVariableDefinitionRendered.measurementType?.title).isEqualTo(title)
        }
    }
}

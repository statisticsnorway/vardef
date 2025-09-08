package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.conversions.StatisticalSubjects
import no.ssb.metadata.vardef.integrations.vardok.conversions.convertSubjectArea
import no.ssb.metadata.vardef.integrations.vardok.conversions.specialSubjectFieldsMapping
import no.ssb.metadata.vardef.integrations.vardok.utils.BaseVardokTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ConvertSubjectAreaTest : BaseVardokTest() {
    companion object {
        @JvmStatic
        fun subjectArea(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "Regionale",
                    StatisticalSubjects.POPULATION,
                ),
                arguments(
                    "region",
                    StatisticalSubjects.POPULATION,
                ),
                arguments(
                    "Valg",
                    StatisticalSubjects.ELECTIONS,
                ),
                arguments(
                    "Vann",
                    StatisticalSubjects.NATURE_AND_THE_ENVIRONMENT,
                ),
                arguments(
                    "Barnevern",
                    StatisticalSubjects.SOCIAL_CONDITIONS_WELFARE_AND_CRIME,
                ),
                arguments(
                    "Skatt",
                    StatisticalSubjects.INCOME_AND_CONSUMPTION,
                ),
                arguments(
                    "Sosiale tjenester, trygd og sosialhjelp",
                    StatisticalSubjects.SOCIAL_CONDITIONS_WELFARE_AND_CRIME,
                ),
                arguments(
                    "Elektrisitets-, gass-, damp- og varmtvannsforsyning",
                    StatisticalSubjects.ENERGY_AND_MANUFACTURING,
                ),
                arguments(
                    "Arbeidsledighet",
                    StatisticalSubjects.LABOUR_MARKET_AND_EARNINGS,
                ),
                arguments(
                    "Hubba hubba",
                    null,
                ),
            )

        @JvmStatic
        fun specialSubjectFields(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "2303",
                    listOf(StatisticalSubjects.BANKING_AND_FINANCIAL_MARKETS),
                ),
                arguments(
                    "3397",
                    listOf(StatisticalSubjects.ENERGY_AND_MANUFACTURING),
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("subjectArea")
    fun `map subject area to subject fields`(
        name: String,
        code: StatisticalSubjects?,
    ) {
        assertThat(convertSubjectArea(name)).isEqualTo(code)
    }

    @ParameterizedTest
    @MethodSource("specialSubjectFields")
    fun `map special cases statistical unit to unit types`(
        id: String,
        code: List<StatisticalSubjects?>,
    ) {
        assertThat(specialSubjectFieldsMapping(id)).isEqualTo(code)
    }
}

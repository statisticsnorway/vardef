package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.convertions.convertUnitTypes
import no.ssb.metadata.vardef.integrations.vardok.convertions.specialCaseUnitMapping
import no.ssb.metadata.vardef.integrations.vardok.utils.BaseVardokTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ConvertionsTest : BaseVardokTest() {
    companion object {
        @JvmStatic
        fun unitTypes(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "Adresse",
                    listOf("01"),
                ),
                arguments(
                    "Arbeidsulykke",
                    listOf("02"),
                ),
                arguments(
                    "Tinglyst omsetning",
                    listOf("05"),
                ),
                arguments(
                    "Skogareal",
                    listOf("05"),
                ),
                arguments(
                    "Grunneiendom",
                    listOf("05"),
                ),
                arguments(
                    "Eiendom",
                    listOf("05"),
                ),
                arguments(
                    "Sak",
                    listOf("12", "13"),
                ),
                arguments(
                    "Fiskefartøy",
                    listOf("21"),
                ),
                arguments(
                    "Internett-abonnement",
                    listOf("27"),
                ),
                arguments(
                    "Repr.vare og -tjeneste",
                    listOf("27"),
                ),
                arguments(
                    "Offentlig forvaltning",
                    listOf("22"),
                ),
                arguments(
                    "Verksemd",
                    listOf("13"),
                ),
                arguments(
                    "Hushald",
                    listOf("10"),
                ),
            )

        @JvmStatic
        fun specialUnitTypes(): Stream<Arguments> =
            Stream.of(
                arguments(
                    "2183",
                    listOf("01"),
                ),
                arguments(
                    "2142",
                    listOf("04"),
                ),
                arguments(
                    "2206",
                    listOf("05"),
                ),
                arguments(
                    "1704",
                    listOf("05"),
                ),
                arguments(
                    "1943",
                    listOf("12", "13"),
                ),
                arguments(
                    "1320",
                    listOf("16"),
                ),
                arguments(
                    "2190",
                    listOf("20"),
                ),
                arguments(
                    "1997",
                    listOf("29"),
                ),
                arguments(
                    "2218",
                    listOf("01", "04", "05"),
                ),
                arguments(
                    "2182",
                    listOf("12", "20"),
                ),
                arguments(
                    "1326",
                    listOf("16", "17"),
                ),
                arguments(
                    "3125",
                    listOf("21"),
                ),
                arguments(
                    "590",
                    listOf("12", "13", "20"),
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("unitTypes")
    fun `map statistical unit to unit types`(
        name: String,
        code: List<String?>,
    ) {
        assertThat(convertUnitTypes(name)).isEqualTo(code)
    }

    @ParameterizedTest
    @MethodSource("specialUnitTypes")
    fun `map special cases statistical unit to unit types`(
        id: String,
        code: List<String?>,
    ) {
        assertThat(specialCaseUnitMapping(id)).isEqualTo(code)
    }
}

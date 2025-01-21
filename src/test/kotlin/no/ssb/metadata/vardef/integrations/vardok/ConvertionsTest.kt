package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.convertions.convertUnitTypes
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
}

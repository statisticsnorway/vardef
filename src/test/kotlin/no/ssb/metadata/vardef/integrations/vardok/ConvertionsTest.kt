package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.utils.BaseVardokTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ConvertionsTest : BaseVardokTest() {
    @Test
    fun `convert statistical unit to unit types`() {
        val statisticalUnit = "Offentlig forvaltning"
        val convertedValue = convertUnitTypes(statisticalUnit)
        assertThat(convertedValue).isEqualTo(listOf("22"))
    }

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
                    listOf("05", "13"),
                ),
                arguments(
                    "Fiskefartøy",
                    listOf("21"),
                ),
                arguments(
                    "Hubba hubba",
                    emptyList<String?>(),
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("unitTypes")
    fun `map statistical unit to unit types`(
        name: String,
        code: List<String?>,
    ) {
        val resultAdresse = convertUnitTypes(name)
        assertThat(resultAdresse).isNotNull
        assertThat(resultAdresse).isEqualTo(code)
    }
}

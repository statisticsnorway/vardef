package no.ssb.metadata.vardef.integrations.vardok

import no.ssb.metadata.vardef.integrations.vardok.convertions.convertSubjectArea
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
                    "be",
                ),
                arguments(
                    "Valg",
                    "va",
                ),
                arguments(
                    "Vann",
                    "nm",
                ),
                arguments(
                    "Barnevern",
                    "sk",
                ),
                arguments(
                    "Skatt",
                    "if",
                ),
                arguments(
                    "Sosiale tjenester, trygd og sosialhjelp",
                    "sk",
                ),
                arguments(
                    "Elektrisitets-, gass-, damp- og varmtvannsforsyning",
                    "ei",
                ),
                arguments(
                    "Arbeidsledighet",
                    "al",
                ),
                arguments(
                    "Hubba hubba",
                    null,
                ),
                arguments(
                    "Jordbruk, jakt, viltstell",
                    null,
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("subjectArea")
    fun `map subject area to subject fields`(
        name: String,
        code: String?,
    ) {
        assertThat(convertSubjectArea(name)).isEqualTo(code)
    }
}

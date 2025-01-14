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
                    "12",
                ),
                arguments(
                    "Valg",
                    "19",
                ),
                arguments(
                    "Vann",
                    "12",
                ),
                arguments(
                    "Barnevern",
                    "15",
                ),
                arguments(
                    "Skatt",
                    "08",
                ),
                arguments(
                    "Sosiale tjenester, trygd og sosialhjelp",
                    "15",
                ),
                arguments(
                    "Elektrisitets-, gass-, damp- og varmtvannsforsyning",
                    "05",
                ),
                arguments(
                    "Arbeidsledighet",
                    "01",
                ),
                arguments(
                    "Hubba hubba",
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

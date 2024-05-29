package no.ssb.metadata

import no.ssb.metadata.models.LanguageStringType
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionTest {
    private lateinit var variableDefinition: VariableDefinitionDAO

    @BeforeAll
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                LanguageStringType(nb = "Norsk navn", nn = "namn", en = "English name"),
                "test",
                LanguageStringType(nb = "definisjon", nn = "nynorsk definisjon", en = "definition"),
            )
    }

    @ParameterizedTest
    @CsvSource(
        "EN, English name",
        "NN, namn",
        "NB, Norsk navn"
    )
    fun `get variable name by language code`(languageCode: SupportedLanguages, expectedName: String) {
        val result = variableDefinition.name.getValidLanguage(languageCode)
        assertThat(result).isEqualTo(expectedName)
    }

    @ParameterizedTest
    @CsvSource(
        "EN, definition",
        "NN, nynorsk definisjon",
        "NB, definisjon"
    )
    fun `get variable definition by language code`(languageCode: SupportedLanguages, expectedDefinition: String) {
        val result = variableDefinition.definition.getValidLanguage(languageCode)
        assertThat(result).isEqualTo(expectedDefinition)
    }
}

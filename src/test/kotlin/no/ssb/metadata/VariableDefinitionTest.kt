package no.ssb.metadata

import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionTest {
    private lateinit var variableDefinition: VariableDefinitionDAO

    @BeforeAll
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf(
                    (SupportedLanguages.NB to "Norsk navn"),
                    (SupportedLanguages.EN to "English name"),
                    (SupportedLanguages.NN to "namn"),
                ),
                "test",
                mapOf(
                    (SupportedLanguages.NB to "definisjon"),
                    (SupportedLanguages.EN to "definition"),
                    (SupportedLanguages.NN to "nynorsk definisjon"),
                ),
            )
    }

    @Test
    fun `get variable name by language code`() {
        val resultNorwegian = variableDefinition.getName("nb")
        val resultEnglish = variableDefinition.getName("en")
        val resultNyNorsk = variableDefinition.getName("nn")
        val incorrectLanguage = variableDefinition.getName("dk")
        assertThat(resultNorwegian).isEqualTo("Norsk navn")
        assertThat(resultEnglish).isEqualTo("English name")
        assertThat(resultNyNorsk).isEqualTo("namn")
        assertThat(incorrectLanguage).isNull()
    }

    @Test
    fun `get variable definition by language_code`() {
        val resultNorwegian = variableDefinition.getDefinition("nb")
        val resultEnglish = variableDefinition.getDefinition("en")
        val resultNyNorsk = variableDefinition.getDefinition("nn")
        val resultIncorrectLanguage = variableDefinition.getDefinition("sv")
        assertThat(resultNorwegian).isEqualTo("definisjon")
        assertThat(resultEnglish).isEqualTo("definition")
        assertThat(resultNyNorsk).isEqualTo("nynorsk definisjon")
        assertThat(resultIncorrectLanguage).isNull()
    }
}

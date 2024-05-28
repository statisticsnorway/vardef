package no.ssb.metadata

import no.ssb.metadata.models.LanguageStringType
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
                LanguageStringType(nb="Norsk navn", nn="namn", en="English name"),
                "test",
                LanguageStringType(nb="definisjon", nn="nynorsk definisjon", en="definition"),
            )
    }

    @Test
    fun `get variable name by language code`() {
        val resultNorwegian = variableDefinition.name.getValidLanguage("nb")
        val resultEnglish = variableDefinition.name.getValidLanguage("en")
        val resultNyNorsk = variableDefinition.name.getValidLanguage("nn")
        val incorrectLanguage = variableDefinition.name.getValidLanguage("dk")
        assertThat(resultNorwegian).isEqualTo("Norsk navn")
        assertThat(resultEnglish).isEqualTo("English name")
        assertThat(resultNyNorsk).isEqualTo("namn")
        assertThat(incorrectLanguage).isNull()
    }

    @Test
    fun `get variable definition by language_code`() {
        val resultNorwegian = variableDefinition.definition.getValidLanguage("nb")
        val resultEnglish = variableDefinition.definition.getValidLanguage("en")
        val resultNyNorsk = variableDefinition.definition.getValidLanguage("nn")
        val resultIncorrectLanguage = variableDefinition.definition.getValidLanguage("sv")
        assertThat(resultNorwegian).isEqualTo("definisjon")
        assertThat(resultEnglish).isEqualTo("definition")
        assertThat(resultNyNorsk).isEqualTo("nynorsk definisjon")
        assertThat(resultIncorrectLanguage).isNull()
    }
}

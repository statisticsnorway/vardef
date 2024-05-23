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
                mapOf(SupportedLanguages.NB to "Norsk navn", SupportedLanguages.EN to "English name"),
                "test",
                mapOf(
                    SupportedLanguages.NB to "definisjon",
                ),
            )
    }

    @Test
    fun testGetName() {
        val resultNorwegian = variableDefinition.getName("nb")
        val resultEnglish = variableDefinition.getName("en")
        val nameNorwegian = "Norsk navn"
        val nameEnglish = "English name"
        assertThat(resultNorwegian).isEqualTo(nameNorwegian)
        assertThat(resultEnglish).isEqualTo(nameEnglish)
    }

    @Test
    fun testGetDefinition() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf(SupportedLanguages.NB to "Norsk navn", SupportedLanguages.EN to "English name"),
                "testNavn",
                mapOf(
                    SupportedLanguages.EN to "Bank definition",
                    SupportedLanguages.NB to "Bankens rolle i verden",
                ),
            )
        val resultNorwegian = variableDefinition.getDefinition("nb")
        val norwegianDefinition = "Bankens rolle i verden"
        assertThat(resultNorwegian).isEqualTo(norwegianDefinition)
    }
}

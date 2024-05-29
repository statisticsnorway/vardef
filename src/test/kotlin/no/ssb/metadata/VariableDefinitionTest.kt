package no.ssb.metadata

import no.ssb.metadata.models.LanguageStringType
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.properties.Delegates

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionTest {
    private lateinit var variableDefinition: VariableDefinitionDAO
    private var nanoIdSize by Delegates.notNull<Int>()

    @BeforeAll
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                LanguageStringType(nb = "Norsk navn", nn = "namn", en = "English name"),
                "test",
                LanguageStringType(nb = "definisjon", nn = "nynorsk definisjon", en = "definition"),
            )
        nanoIdSize = 8
    }

    @ParameterizedTest
    @CsvSource(
        "EN, English name",
        "NN, namn",
        "NB, Norsk navn",
    )
    fun `get variable name by language code`(
        languageCode: SupportedLanguages,
        expectedName: String,
    ) {
        val result = variableDefinition.name.getValidLanguage(languageCode)
        assertThat(result).isEqualTo(expectedName)
    }

    @ParameterizedTest
    @CsvSource(
        "EN, definition",
        "NN, nynorsk definisjon",
        "NB, definisjon",
    )
    fun `get variable definition by language code`(
        languageCode: SupportedLanguages,
        expectedDefinition: String,
    ) {
        val result = variableDefinition.definition.getValidLanguage(languageCode)
        assertThat(result).isEqualTo(expectedDefinition)
    }

    @Test
    fun `variable definition id is created`() {
        assertThat(variableDefinition.id).isNotNull()
    }

    @Test
    fun `variable definition id persists through updates`() {
        val initialId = variableDefinition.id
        val initialShortName = variableDefinition.shortName
        variableDefinition.shortName = "test1"
        assertThat(initialShortName).isNotSameAs(variableDefinition.shortName)
        assertThat(initialId).isEqualTo(variableDefinition.id)
    }

    @Test
    fun `variable definition id is expected length`() {
        val nanoId = variableDefinition.id
        if (nanoId != null) {
            assertThat(nanoId.length).isEqualTo(nanoIdSize)
        }
    }
}

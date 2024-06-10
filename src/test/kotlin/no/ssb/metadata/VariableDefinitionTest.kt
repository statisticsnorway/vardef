package no.ssb.metadata

import io.viascom.nanoid.NanoId
import no.ssb.metadata.models.LanguageStringType
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.types.ObjectId
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
                id = ObjectId(),
                definitionId = NanoId.generate(8),
                name = LanguageStringType(nb = "Norsk navn", nn = "namn", en = "English name"),
                shortName = "test",
                definition = LanguageStringType(nb = "definisjon", nn = "nynorsk definisjon", en = "definition"),
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
        assertThat(variableDefinition.definitionId).isNotNull()
    }

    @Test
    fun `variable definition id is expected length`() {
        val nanoId = variableDefinition.definitionId
        assertThat(nanoId?.length).isEqualTo(nanoIdSize)
    }
}

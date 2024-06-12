package no.ssb.metadata

import SAVED_VARIABLE_DEFINITION
import no.ssb.metadata.models.SavedVariableDefinition
import no.ssb.metadata.models.SupportedLanguages
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.properties.Delegates

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionTest {
    private lateinit var variableDefinition: SavedVariableDefinition
    private var nanoIdSize by Delegates.notNull<Int>()

    @BeforeAll
    fun setUp() {
        variableDefinition = SAVED_VARIABLE_DEFINITION
        nanoIdSize = 8
    }

    @ParameterizedTest
    @CsvSource(
        "EN, Country Background",
        "NN, Landbakgrunn",
        "NB, Landbakgrunn",
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
        "EN, Country background is",
        "NN, For personer født",
        "NB, For personer født",
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

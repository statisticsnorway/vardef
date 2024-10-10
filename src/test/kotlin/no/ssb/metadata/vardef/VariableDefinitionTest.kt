package no.ssb.metadata.vardef

import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.COMPLETE_RESPONSE
import no.ssb.metadata.vardef.utils.RENDERED_VARIABLE_DEFINITION_NULL_CONTACT
import no.ssb.metadata.vardef.utils.SAVED_TAX_EXAMPLE
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
    private lateinit var renderedVariableDefinition: RenderedVariableDefinition
    private lateinit var completeResponseVariableDefinition: CompleteResponse

    @BeforeAll
    fun setUp() {
        variableDefinition = SAVED_TAX_EXAMPLE
        nanoIdSize = 8
        renderedVariableDefinition = RENDERED_VARIABLE_DEFINITION_NULL_CONTACT
        completeResponseVariableDefinition = COMPLETE_RESPONSE
    }

    @ParameterizedTest
    @CsvSource(
        "EN, Income tax",
        "NN, Inntektsskatt",
        "NB, Inntektsskatt",
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
        "EN, Income tax",
        "NN, Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
        "NB, Inntektsskatt utlignes til staten på grunnlag av alminnelig inntekt.",
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
        assertThat(nanoId.length).isEqualTo(nanoIdSize)
    }

    @Test
    fun `rendered variable without contact information`() {
        assertThat(renderedVariableDefinition.contact).isNull()
        assertThat(renderedVariableDefinition.name).isEqualTo("Landbakgrunn")
    }

    @Test
    fun `complete response include owner`() {
        assertThat(completeResponseVariableDefinition).hasFieldOrProperty("owner")
    }
}

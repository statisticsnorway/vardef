package no.ssb.metadata

import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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
        nanoIdSize = 8
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `get variable name by language code`(language: SupportedLanguages) {
        val result = variableDefinition.getName(language)
        assertThat(result).isEqualTo(variableDefinition.name[language])
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `get variable definition by language code`(language: SupportedLanguages) {
        val result = variableDefinition.getDefinition(language)
        assertThat(result).isEqualTo(variableDefinition.definition[language])
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

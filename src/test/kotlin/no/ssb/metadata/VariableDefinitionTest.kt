package no.ssb.metadata

import no.ssb.metadata.models.LanguageStringType
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

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

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `get variable name by language code`(language: SupportedLanguages) {
        val result = variableDefinition.name.getValidLanguage(language)
        assertThat(result).isEqualTo(variableDefinition.name.getValidLanguage(language))
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `get variable definition by language code`(language: SupportedLanguages) {
        val result = variableDefinition.definition.getValidLanguage(language)
        assertThat(result).isEqualTo(variableDefinition.definition.getValidLanguage(language))
    }
}

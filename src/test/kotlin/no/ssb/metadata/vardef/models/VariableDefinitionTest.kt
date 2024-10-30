package no.ssb.metadata.vardef.models

import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.URI
import java.time.LocalDate
import kotlin.properties.Delegates

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionTest {
    private lateinit var variableDefinition: SavedVariableDefinition
    private var nanoIdSize by Delegates.notNull<Int>()
    private lateinit var renderedVariableDefinition: RenderedVariableDefinition
    private lateinit var completeResponseVariableDefinition: CompleteResponse

    private val draftExample =
        Draft(
            name =
                LanguageStringType(
                    nb = "Fly",
                    nn = null,
                    en = "Airplane",
                ),
            shortName = "fly",
            definition =
                LanguageStringType(
                    nb = "Et transportmiddel med vinger.",
                    nn = null,
                    en = "A means of transportation with wings",
                ),
            classificationReference = "91",
            unitTypes = listOf("", ""),
            subjectFields = listOf("", ""),
            containsSensitivePersonalInformation = false,
            measurementType = "",
            validFrom = LocalDate.of(1988, 5, 17),
            externalReferenceUri = URI("https://www.example.com").toURL(),
            comment = null,
            relatedVariableDefinitionUris = listOf(URI("https://www.example.com").toURL()),
            contact =
                Contact(
                    LanguageStringType("", "", ""),
                    "",
                ),
        )

    @BeforeAll
    fun setUp() {
        variableDefinition = INCOME_TAX_VP1_P1
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

    @ParameterizedTest
    @CsvSource(
        "play-enhjoern-a-developers, play-enhjoern-a",
        "play-fix-data-admins, play-fix",
        "skips-data-managers, skips-data",
    )
    fun `owner team is substring of group name`(
        group: String,
        expectedteam: String,
    ) {
        val savedVariableDefinition = draftExample.toSavedVariableDefinition(group)
        assertThat(savedVariableDefinition.owner.team).isEqualTo(expectedteam)
    }
}

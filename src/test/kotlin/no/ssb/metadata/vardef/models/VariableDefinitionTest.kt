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
    private lateinit var renderedView: RenderedView
    private lateinit var completeViewVariableDefinition: CompleteView

    private val createDraftExample =
        CreateDraft(
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
            containsSpecialCategoriesOfPersonalData = false,
            measurementType = "",
            validFrom = LocalDate.of(1988, 5, 17),
            validUntil = null,
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
        renderedView = RENDERED_VIEW_NULL_CONTACT
        completeViewVariableDefinition = COMPLETE_VIEW
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
        val result = variableDefinition.name.getValue(languageCode)
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
        val result = variableDefinition.definition.getValue(languageCode)
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
        assertThat(renderedView.contact).isNull()
        assertThat(renderedView.name).isEqualTo("Landbakgrunn")
    }

    @Test
    fun `complete view include owner`() {
        assertThat(completeViewVariableDefinition).hasFieldOrProperty("owner")
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
        val savedVariableDefinition = createDraftExample.toSavedVariableDefinition(group, TEST_USER)
        assertThat(savedVariableDefinition.owner.team).isEqualTo(expectedteam)
    }
}

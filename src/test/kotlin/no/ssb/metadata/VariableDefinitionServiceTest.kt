package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionServiceTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    private lateinit var variableDefinition: VariableDefinitionDAO
    private lateinit var variables: List<VariableDefinitionDAO>

    @BeforeEach
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi"), (SupportedLanguages.EN to "value")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinitionService.save(variableDefinition)
        // variables = variableDefinitionService.findAll()
    }

    @Test
    fun get_variable_definition_with_no_value_in_selected_language() {
        val variablesNyNorsk = variableDefinitionService.findByLanguage("nn")
        assertThat(variablesNyNorsk[0].shortName).isEqualTo("test1")
        assertThat(variablesNyNorsk[0].name).isNull()
    }

    @Test
    fun save_variable_definition() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi 2"), (SupportedLanguages.EN to "value 2")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon 2"), (SupportedLanguages.EN to "definition 2")),
            )
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result.id).isNotNull()
    }
}

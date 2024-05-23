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
        variables = variableDefinitionService.findAll()
    }

    @Test
    fun testGetVariableDefinitionMissingValuesInSelectedLanguage() {
        val variablesNyNorsk = variableDefinitionService.findByLanguage("nn")
        assertThat(variablesNyNorsk[0].shortName).isEqualTo("test1")
        assertThat(variablesNyNorsk[0].name).isNull()
        assertThat(variablesNyNorsk[0]).isNotNull()
        assertThat(variables[0].shortName).isEqualTo(variablesNyNorsk[0].shortName)
    }
}

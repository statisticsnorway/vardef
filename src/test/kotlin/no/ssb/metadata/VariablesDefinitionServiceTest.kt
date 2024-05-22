package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesDefinitionServiceTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @BeforeEach
    fun setUp() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi"), (SupportedLanguages.EN to "value")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinitionService.save(variableDefinition)
    }

    @Test
    fun testFindByLanguage() {
        val all = variableDefinitionService.findAll()
        val variables = variableDefinitionService.findByLanguage("nn")
        assertThat(variables[0].shortName).isEqualTo("test1")
        assertThat(variables[0].name).isNull()
        assertThat(all[0].shortName).isEqualTo("test1")
    }
}

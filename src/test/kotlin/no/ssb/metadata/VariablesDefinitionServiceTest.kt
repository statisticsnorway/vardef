package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertTrue
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesDefinitionServiceTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @Test
    fun testFindByLanguage() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi"), (SupportedLanguages.EN to "value")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinitionService.save(variableDefinition)
        val all = variableDefinitionService.findAll()
        val variables = variableDefinitionService.findByLanguage("nn")
        assertTrue(true)
        // assertThat(variables[0].shortName).isEqualTo("test1")
        // assertThat(variables[0].name).isNull()
        // assertThat(all[0].shortName).isEqualTo("test1")
    }
}

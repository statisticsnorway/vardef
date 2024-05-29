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
class VariableDefinitionIdTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService
    private lateinit var variableDefinition: VariableDefinitionDAO
    private lateinit var variableDefinition1: VariableDefinitionDAO

    @BeforeEach
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "Bilturer"), (SupportedLanguages.EN to "Road trips")),
                "bil",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinition1 =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "gåturer"), (SupportedLanguages.EN to "Hiking")),
                "tur",
                mapOf((SupportedLanguages.NB to "Ut på tur"), (SupportedLanguages.EN to "Walking about")),
            )
    }

    @Test
    fun `varDef id is generated when variable is created`() {
        val saveVariable = variableDefinitionService.save(variableDefinition1)
        assertThat(saveVariable.id).isNotNull()
        assertThat(saveVariable.id).isEqualTo(variableDefinition1.id)
    }

    @Test
    fun `varDef id is only created once`() {
        val idBeforeSave = variableDefinition.id
        val shortNameBeforeSave = variableDefinition.shortName
        variableDefinition.shortName = "bilSport"
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(idBeforeSave).isSameAs(result.id)
        assertThat(shortNameBeforeSave).isNotSameAs(result.shortName)
    }

    @Test
    fun `all variables has mongodb id`() {
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result.objectId).isNotNull()
        variableDefinition.shortName = "campingbil"
        val result2 = variableDefinitionService.save(variableDefinition)
        assertThat(result2.objectId).isNotSameAs(result.objectId)
        assertThat(result2.id).isSameAs(result.id)
    }
}

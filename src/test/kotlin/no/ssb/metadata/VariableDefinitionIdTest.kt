package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.Assertions
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

    @BeforeEach
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "Transaksjon"), (SupportedLanguages.EN to "Transition")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )}

        @Test
        fun `varDef id is only created once`() {
            val idBeforeSave = variableDefinition.id
            val shortNameBeforeSave = variableDefinition.shortName
            variableDefinition.shortName = "bankUtgang"
            val result = variableDefinitionService.save(variableDefinition)
            assertThat(idBeforeSave).isSameAs(result.id)
            assertThat(shortNameBeforeSave).isNotSameAs(result.shortName)
        }

        @Test
        fun `all variables has mongodb id`() {
            val variableDefinition1 =
                VariableDefinitionDAO(
                    null,
                    mapOf(SupportedLanguages.NB to "bilturer"),
                    "bil",
                    mapOf(SupportedLanguages.NB to "Bil som kjøres på turer"),
                )
            val result = variableDefinitionService.save(variableDefinition1)
            assertThat(result.objectId).isNotNull()
            variableDefinition1.shortName = "campingbil"
            val result2 = variableDefinitionService.save(variableDefinition1)
            assertThat(result2.objectId).isNotSameAs(result.objectId)
            assertThat(result2.id).isSameAs(result.id)
        }
}
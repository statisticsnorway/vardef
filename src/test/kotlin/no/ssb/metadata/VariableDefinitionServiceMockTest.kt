package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.repositories.VariableDefinitionRepository
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

@MockK
class VariableDefinitionServiceMockTest {
    private val variableDefinitionRepository = mockk<VariableDefinitionRepository>()
    private val variableDefinitionService = VariableDefinitionService(variableDefinitionRepository)

    @Test
    fun find_all_variables_no_data() {
        every {
            variableDefinitionRepository.findAll()
        } returns emptyList()
        val result = variableDefinitionService.findAll()
        assertTrue(result.isEmpty())
        verify(exactly = 1) { variableDefinitionRepository.findAll() }
    }

    @Test
    fun save_variable_definition() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf(SupportedLanguages.NB to "Kattens gange"),
                "katt",
                mapOf(SupportedLanguages.NB to "Katter går på fire bein."),
            )
        every {
            variableDefinitionService.save(variableDefinition)
        } returns variableDefinition
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result).isEqualTo(variableDefinition)
    }
}

package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.repositories.VariableDefinitionRepository
import no.ssb.metadata.services.VariableDefinitionService
import org.junit.jupiter.api.Test

class VariableDefinitionServiceMockTest {
    private val variableDefinitionRepository = mockk<VariableDefinitionRepository>()
    private val variableDefinitionService = VariableDefinitionService(variableDefinitionRepository)

    @Test
    fun `should return empty list`() {
        every {
            variableDefinitionRepository.findAll()
        } returns emptyList()
        val result = variableDefinitionService.findAll()
        assertTrue(result.isEmpty())
        verify(exactly = 1) { variableDefinitionRepository.findAll() }
    }
}

package no.ssb.metadata

import RENDERED_VARIABLE_DEFINITION
import SAVED_VARIABLE_DEFINITION
import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.repositories.VariableDefinitionRepository
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MockK
class VariableDefinitionServiceTest {
    private lateinit var variableDefinitionMockRepository: VariableDefinitionRepository
    private lateinit var variableDefinitionService: VariableDefinitionService

    @BeforeEach
    fun setUp() {
        variableDefinitionMockRepository = mockk<VariableDefinitionRepository>()
        variableDefinitionService = VariableDefinitionService(variableDefinitionMockRepository)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `find all variables no data`() {
        every {
            variableDefinitionMockRepository.findAll()
        } returns emptyList()
        val result = variableDefinitionService.listAll()
        assertTrue(result.isEmpty())
        verify(exactly = 1) { variableDefinitionMockRepository.findAll() }
    }

    @Test
    fun `save variable definition`() {
        val variableDefinition = SAVED_VARIABLE_DEFINITION
        val savedVariableDefinition =
            SAVED_VARIABLE_DEFINITION.copy(
                definitionId = "8Ah4fbvb",
                id = ObjectId("00000020f51bb4362eee2a4d"),
            )

        every {
            variableDefinitionService.save(variableDefinition)
        } returns savedVariableDefinition
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result).isEqualTo(savedVariableDefinition)
        assertThat(result.name).isEqualTo(savedVariableDefinition.name)
        assertThat(result.id).isEqualTo(savedVariableDefinition.id)
        assertThat(result.definitionId).isEqualTo(savedVariableDefinition.definitionId)
    }

    @Test
    fun `find variables in selected language`() {
        val variableDefinition = SAVED_VARIABLE_DEFINITION

        every { variableDefinitionMockRepository.findAll() } returns listOf(variableDefinition)

        val renderedVariableDefinition = RENDERED_VARIABLE_DEFINITION.copy(id = variableDefinition.definitionId)

        val result = variableDefinitionService.listAllAndRenderForLanguage(SupportedLanguages.NB)
        assertThat(result.isNotEmpty())
        assertThat(listOf(renderedVariableDefinition).map { it.id }).isEqualTo(result.map { it.id })
        assertThat(result[0].id).isEqualTo(renderedVariableDefinition.id)
        verify { variableDefinitionMockRepository.findAll() }
    }

    @Test
    fun `mongodb id is generated when variable is created`() {
        val variableDefinition = SAVED_VARIABLE_DEFINITION.copy(id = null)

        val savedVariableDefinition = variableDefinition.copy(id = ObjectId.get())

        every { variableDefinitionService.save(variableDefinition) } returns savedVariableDefinition
        assertThat(variableDefinition.id).isNull()

        val saveVariable = variableDefinitionService.save(variableDefinition)
        assertThat(saveVariable.id).isNotNull()
    }

    @Test
    fun `varDef id is only created once`() {
        val variableDefinition = SAVED_VARIABLE_DEFINITION.copy(definitionId = "y7s34rf1")

        val idBeforeSave = variableDefinition.definitionId
        val shortNameBeforeSave = variableDefinition.shortName

        val savedVariableDefinition = variableDefinition.copy(shortName = "food")

        every { variableDefinitionService.save(variableDefinition) } returns savedVariableDefinition

        val result = variableDefinitionService.save(variableDefinition)
        assertThat(idBeforeSave).isSameAs(result.definitionId)
        assertThat(shortNameBeforeSave).isNotSameAs(result.shortName)
    }
}

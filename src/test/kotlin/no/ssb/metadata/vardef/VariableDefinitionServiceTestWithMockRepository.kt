package no.ssb.metadata.vardef

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.RENDERED_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.SAVED_TAX_EXAMPLE
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@MockK
class VariableDefinitionServiceTestWithMockRepository {
    private lateinit var variableDefinitionMockRepository: VariableDefinitionRepository
    private lateinit var variableDefinitionService: VariableDefinitionService
    private lateinit var mockKlassService: KlassService

    @BeforeEach
    fun setUp() {
        variableDefinitionMockRepository = mockk<VariableDefinitionRepository>()
        mockKlassService = mockk<KlassService>()
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
        val variableDefinition = SAVED_TAX_EXAMPLE
        val savedVariableDefinition =
            SAVED_TAX_EXAMPLE.copy(
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
        val variableDefinition = SAVED_TAX_EXAMPLE

        (
            variableDefinitionService::class.java
                .getDeclaredField("klassService")
                .apply { isAccessible = true }
                .set(variableDefinitionService, mockKlassService)
        )

        every {
            mockKlassService.getCodeItemFor(any(), any(), any())
        } returns
            KlassReference("https://www.ssb.no/en/klass/klassifikasjoner/91", "01", "Adresse")

        every {
            mockKlassService.getKlassUrlForIdAndLanguage(any(), any())
        } returns "https://www.ssb.no/en/klass/klassifikasjoner/91"

        every { variableDefinitionMockRepository.findAll() } returns listOf(variableDefinition)

        val renderedVariableDefinition = RENDERED_VARIABLE_DEFINITION.copy(id = variableDefinition.definitionId)

        val result =
            variableDefinitionService.listAllAndRenderForLanguage(SupportedLanguages.NB, LocalDate.now())
        assertThat(result.isNotEmpty())
        assertThat(result.size).isEqualTo(1)
        assertThat(listOf(renderedVariableDefinition).map { it.id }).isEqualTo(result.map { it.id })
        assertThat(result[0].id).isEqualTo(renderedVariableDefinition.id)
        verify { variableDefinitionMockRepository.findAll() }
    }

    @Test
    fun `mongodb id is generated when variable is created`() {
        val variableDefinition = SAVED_TAX_EXAMPLE.copy(id = null)

        val savedVariableDefinition = variableDefinition.copy(id = ObjectId.get())

        every { variableDefinitionService.save(variableDefinition) } returns savedVariableDefinition
        assertThat(variableDefinition.id).isNull()

        val saveVariable = variableDefinitionService.save(variableDefinition)
        assertThat(saveVariable.id).isNotNull()
    }

    @Test
    fun `varDef id is only created once`() {
        val variableDefinition = SAVED_TAX_EXAMPLE.copy(definitionId = "y7s34rf1")

        val idBeforeSave = variableDefinition.definitionId
        val shortNameBeforeSave = variableDefinition.shortName

        val savedVariableDefinition = variableDefinition.copy(shortName = "food")

        every { variableDefinitionService.save(variableDefinition) } returns savedVariableDefinition

        val result = variableDefinitionService.save(variableDefinition)
        assertThat(idBeforeSave).isSameAs(result.definitionId)
        assertThat(shortNameBeforeSave).isNotSameAs(result.shortName)
    }
}

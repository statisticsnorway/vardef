package no.ssb.metadata.vardef.services

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
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.RENDERED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

@MockK
class VariableDefinitionServiceTestWithMockRepository {
    private lateinit var variableDefinitionMockRepository: VariableDefinitionRepository
    private lateinit var variableDefinitionService: VariableDefinitionService
    private lateinit var mockKlassService: KlassService
    private lateinit var mockPatchesService: PatchesService
    private lateinit var mockValidityPeriodsService: ValidityPeriodsService

    @BeforeEach
    fun setUp() {
        variableDefinitionMockRepository = mockk<VariableDefinitionRepository>()
        mockKlassService = mockk<KlassService>()
        mockPatchesService = mockk<PatchesService>()
        mockValidityPeriodsService = mockk<ValidityPeriodsService>()
        variableDefinitionService =
            VariableDefinitionService(variableDefinitionMockRepository, mockPatchesService, mockValidityPeriodsService)
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
        val result = variableDefinitionService.list()
        assertTrue(result.isEmpty())
        verify(exactly = 1) { variableDefinitionMockRepository.findAll() }
    }

    @Test
    fun `find variables in selected language`() {
        val variableDefinition = INCOME_TAX_VP1_P1
        val today = LocalDate.now()

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

        every { mockValidityPeriodsService.getForDate(variableDefinition.definitionId, today) } returns variableDefinition

        val renderedVariableDefinition = RENDERED_VARIABLE_DEFINITION.copy(id = variableDefinition.definitionId)

        val result =
            variableDefinitionService.listForDateAndRender(SupportedLanguages.NB, today)
        assertThat(result.isNotEmpty())
        assertThat(result.size).isEqualTo(1)
        assertThat(listOf(renderedVariableDefinition).map { it.id }).isEqualTo(result.map { it.id })
        assertThat(result[0].id).isEqualTo(renderedVariableDefinition.id)
        verify { variableDefinitionMockRepository.findAll() }
    }
}

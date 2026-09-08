package no.ssb.metadata.vardef.services

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.*
import no.ssb.metadata.vardef.config.KlassClassificationConfiguration
import no.ssb.metadata.vardef.config.KlassConfiguration
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.RENDERED_VIEW
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
    private lateinit var mockValidityPeriodsService: ValidityPeriodsService
    private lateinit var klassConfiguration: KlassConfiguration

    @BeforeEach
    fun setUp() {
        variableDefinitionMockRepository = mockk<VariableDefinitionRepository>()
        mockKlassService = mockk<KlassService>()
        mockValidityPeriodsService = mockk<ValidityPeriodsService>()
        klassConfiguration =
            KlassConfiguration(
                listOf(
                    KlassClassificationConfiguration("subject-fields").apply {
                        id = "618"
                        date = "today"
                    },
                    KlassClassificationConfiguration("unit-types").apply {
                        id = "702"
                        date = "today"
                    },
                    KlassClassificationConfiguration("measurement-type").apply {
                        id = "303"
                        date = "today"
                        levels = listOf(1)
                    },
                ),
            )
        variableDefinitionService =
            VariableDefinitionService(
                variableDefinitionMockRepository,
                mockKlassService,
                klassConfiguration,
                mockValidityPeriodsService,
            )
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
            mockKlassService.renderCode(any(), any(), any())
        } returns
            KlassReference("https://www.ssb.no/en/klass/klassifikasjoner/91", "01", "Adresse")

        every {
            mockKlassService.getKlassUrlForIdAndLanguage(any(), any())
        } returns "https://www.ssb.no/en/klass/klassifikasjoner/91"

        every {
            variableDefinitionMockRepository.findDistinctDefinitionIdByVariableStatusInList(
                listOf(VariableStatus.PUBLISHED_EXTERNAL),
            )
        } returns
            setOf(variableDefinition.definitionId)

        every { mockValidityPeriodsService.getForDate(variableDefinition.definitionId, today) } returns variableDefinition

        val renderedVariableDefinition = RENDERED_VIEW.copy(id = variableDefinition.definitionId)

        val result =
            variableDefinitionService.listPublicForDate(SupportedLanguages.NB, today)
        assertThat(result.isNotEmpty())
        assertThat(result.size).isEqualTo(1)
        assertThat(listOf(renderedVariableDefinition).map { it.id }).isEqualTo(result.map { it.id })
        assertThat(result[0].id).isEqualTo(renderedVariableDefinition.id)
        verify(exactly = 1) {
            variableDefinitionMockRepository.findDistinctDefinitionIdByVariableStatusInList(
                listOf(VariableStatus.PUBLISHED_EXTERNAL),
            )
        }
    }
}

package no.ssb.metadata.vardef.services

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.RENDERED_VARIABLE_DEFINITION
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import java.time.LocalDate
import kotlin.collections.emptyList

@MockK
@Disabled
class VariableDefinitionServiceTestWithMockRepository {
    private lateinit var variableDefinitionMockRepository: VariableDefinitionRepository
    private lateinit var variableDefinitionService: VariableDefinitionService
    private lateinit var mockKlassService: KlassService
    private lateinit var mockValidityPeriodsService: ValidityPeriodsService

    @BeforeEach
    fun setUp() {
        variableDefinitionMockRepository = mockk<VariableDefinitionRepository>()
        mockKlassService = mockk<KlassService>()
        mockValidityPeriodsService = mockk<ValidityPeriodsService>()
        variableDefinitionService =
            VariableDefinitionService(variableDefinitionMockRepository, mockKlassService, mockValidityPeriodsService)
    }

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `find all variables no data`() {
        every {
            variableDefinitionMockRepository.findAll()
        } returns Mono.empty<SavedVariableDefinition>()
        val result = runBlocking { variableDefinitionService.list() }
        assertTrue(result.isEmpty())
        verify(exactly = 1) { runBlocking { variableDefinitionMockRepository.findAll().asFlow().toList() } }
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
            Mono.just(variableDefinition.definitionId)

        every {
            runBlocking {
                mockValidityPeriodsService.getForDate(variableDefinition.definitionId, today)
            }
        } returns variableDefinition

        val renderedVariableDefinition = RENDERED_VARIABLE_DEFINITION.copy(id = variableDefinition.definitionId)

        val result =
            runBlocking {
                variableDefinitionService.listPublicForDate(SupportedLanguages.NB, today)
            }
        assertThat(result.isNotEmpty())
        assertThat(result.size).isEqualTo(1)
        assertThat(listOf(renderedVariableDefinition).map { it.id }).isEqualTo(result.map { it.id })
        assertThat(result[0].id).isEqualTo(renderedVariableDefinition.id)
        verify(exactly = 1) {
            runBlocking {
                variableDefinitionMockRepository
                    .findDistinctDefinitionIdByVariableStatusInList(
                        listOf(VariableStatus.PUBLISHED_EXTERNAL),
                    ).asFlow()
                    .toList()
            }
        }
    }
}

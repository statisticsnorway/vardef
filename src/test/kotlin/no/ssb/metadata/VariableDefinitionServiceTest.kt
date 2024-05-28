package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.models.VariableDefinitionDTO
import no.ssb.metadata.repositories.VariableDefinitionRepository
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
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
        val result = variableDefinitionService.findAll()
        assertTrue(result.isEmpty())
        verify(exactly = 1) { variableDefinitionMockRepository.findAll() }
    }

    @Test
    fun `save variable definition`() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf(SupportedLanguages.NB to "Kattens gange"),
                "katt",
                mapOf(SupportedLanguages.NB to "Katter går på fire bein."),
            )
        val savedVariableDefinition =
            VariableDefinitionDAO(
                ObjectId("00000020f51bb4362eee2a4d"),
                mapOf(SupportedLanguages.NB to "Kattens gange"),
                "katt",
                mapOf(SupportedLanguages.NB to "Katter går på fire bein."),
                "8Ah4fbvb",
            )
        every {
            variableDefinitionService.save(variableDefinition)
        } returns savedVariableDefinition
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result).isEqualTo(savedVariableDefinition)
    }

    @Test
    fun `find variables in selected language`() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "marsvin sport"), (SupportedLanguages.EN to "guinea pig sport")),
                "marsvin",
                mapOf((SupportedLanguages.NB to "marsvin trener"), (SupportedLanguages.EN to "guinea pig in training")),
            )
        every { variableDefinitionMockRepository.findAll() } returns listOf(variableDefinition)
        val language = "nb"
        val variableDefinitionDTO =
            VariableDefinitionDTO(
                variableDefinition.varDefId,
                "marsvin sport",
                "marsvin",
                "marsvin trener",
            )
        val result = variableDefinitionService.findByLanguage(language)
        assert(result.isNotEmpty())
        assertEquals(listOf(variableDefinitionDTO), result)
        verify { variableDefinitionMockRepository.findAll() }
    }
}

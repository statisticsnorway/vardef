package no.ssb.metadata

import com.mongodb.assertions.Assertions.assertTrue
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import no.ssb.metadata.models.LanguageStringType
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
import org.junit.jupiter.api.assertThrows

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
                name = LanguageStringType(nb = "Kattens gange", nn = null, en = null),
                shortName = "katt",
                definition = LanguageStringType(nb = "Katter går på fire bein.", nn = null, en = null),
            )
        val savedVariableDefinition =
            VariableDefinitionDAO(
                id = "8Ah4fbvb",
                mongoId = ObjectId("00000020f51bb4362eee2a4d"),
                name = LanguageStringType(nb = "Kattens gange", nn = null, en = null),
                shortName = "katt",
                definition = LanguageStringType(nb = "Katter går på fire bein.", nn = null, en = null),
            )
        every {
            variableDefinitionService.save(variableDefinition)
        } returns savedVariableDefinition
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result).isEqualTo(savedVariableDefinition)
        assertThat(result.name).isEqualTo(savedVariableDefinition.name)
        assertThat(result.mongoId).isEqualTo(savedVariableDefinition.mongoId)
        assertThat(result.id).isEqualTo(savedVariableDefinition.id)
    }

    @Test
    fun `find variables in selected language`() {
        val variableDefinition =
            VariableDefinitionDAO(
                name = LanguageStringType(nb = "marsvin sport", nn = null, en = "guinea pig sport"),
                shortName = "marsvin",
                definition = LanguageStringType(nb = "marsvin trener", nn = null, en = "guinea pig in training"),
            )
        every { variableDefinitionMockRepository.findAll() } returns listOf(variableDefinition)
        val variableDefinitionDTO =
            VariableDefinitionDTO(
                variableDefinition.id,
                "marsvin sport",
                "marsvin",
                "marsvin trener",
            )
        val result = variableDefinitionService.findByLanguage(SupportedLanguages.NB)
        assert(result.isNotEmpty())
        assertEquals(listOf(variableDefinitionDTO), result)
        assertThat(result[0].id).isEqualTo(variableDefinitionDTO.id)
        verify { variableDefinitionMockRepository.findAll() }
    }

    @Test
    fun `mongodb id is generated when variable is created`() {
        val variableDefinition =
            VariableDefinitionDAO(
                name = LanguageStringType(nb = "Middag", null, null),
                shortName = "mat",
                definition = LanguageStringType(nb = "Mat man spiser etter jobb", null, null),
            )
        val savedVariableDefinition = variableDefinition.copy(mongoId = ObjectId.get())

        every { variableDefinitionService.save(variableDefinition) } returns savedVariableDefinition
        assertThat(variableDefinition.mongoId).isNull()

        val saveVariable = variableDefinitionService.save(variableDefinition)
        assertThat(saveVariable.mongoId).isNotNull()
    }

    @Test
    fun `varDef id is only created once`() {
        val variableDefinition =
            VariableDefinitionDAO(
                id = "y7s34rf1",
                name = LanguageStringType(nb = null, en = "Supper", nn = null),
                shortName = "englishFood",
                definition = LanguageStringType(nb = null, en = "Food after work", nn = null),
            )
        val idBeforeSave = variableDefinition.id
        val shortNameBeforeSave = variableDefinition.shortName

        val savedVariableDefinition = variableDefinition.copy(shortName = "food")

        every { variableDefinitionService.save(variableDefinition) } returns savedVariableDefinition

        val result = variableDefinitionService.save(variableDefinition)
        assertThat(idBeforeSave).isSameAs(result.id)
        assertThat(shortNameBeforeSave).isNotSameAs(result.shortName)
    }

    @Test
    fun `save should throw exception for null id`() {
        val variableDefinition =
            VariableDefinitionDAO(
                id = null,
                name = LanguageStringType(nb = "navn", en = null, nn = null),
                shortName = "kortNavn",
                definition = LanguageStringType(nb = "definisjon", en = null, nn = null),
            )

        val exception =
            assertThrows<IllegalArgumentException> {
                variableDefinitionService.save(variableDefinition)
            }

        assertEquals(
            "Something went wrong while saving variable, 'id' is missing",
            exception.message,
        )
    }
}

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
import org.junit.jupiter.api.Test

@MockK
class VariableDefinitionServiceTest {
    private val variableDefinitionRepository = mockk<VariableDefinitionRepository>()
    private val variableDefinitionService = VariableDefinitionService(variableDefinitionRepository)

    //@BeforeEach

    @AfterEach
    internal fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `find all variables no data`() {
        every {
            variableDefinitionRepository.findAll()
        } returns emptyList()
        val result = variableDefinitionService.findAll()
        assertTrue(result.isEmpty())
        verify(exactly = 1) { variableDefinitionRepository.findAll() }
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
            )
        every {
            variableDefinitionService.save(variableDefinition)
        } returns savedVariableDefinition
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result).isEqualTo(savedVariableDefinition)
    }

    @Test
    fun `find variables in selected language`(){
        val variableDefinitionDAO = VariableDefinitionDAO(
            ObjectId("00000020f51bb4362eee2a4d"),
            mapOf((SupportedLanguages.NB to "marsvin sport"),(SupportedLanguages.EN to "guinea pig")),
            "marsvin",
            mapOf((SupportedLanguages.NB to "marsvin som trener"),(SupportedLanguages.EN to "guinea pig in training"))
        )
        val variableDefinitionDAO2 = VariableDefinitionDAO(
            ObjectId("00000020f51bb4362eee2a4e"),
            mapOf((SupportedLanguages.NB to "hamster sport"),(SupportedLanguages.EN to "hamster")),
            "marsvin",
            mapOf((SupportedLanguages.NB to "hamster som trener"),(SupportedLanguages.EN to "hamster in training"))
        )
        val variableDefinitionDTO = VariableDefinitionDTO(
            "marsvin sport", "marsvin","marsvin trener"
        )
        val variableDefinitionDTO2 = VariableDefinitionDTO(
            "hamster sport","hamster","hamster trener"
        )
        every {
            variableDefinitionService.findByLanguage("nb")
        } returns listOf(variableDefinitionDTO,variableDefinitionDTO2)
        val result = variableDefinitionService.findByLanguage("nb")
        //assertTrue(result.isEmpty())
        //verify(exactly = 1) { variableDefinitionRepository.findAll() }

    }

    /*
     @BeforeEach
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi"), (SupportedLanguages.EN to "value")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinitionService.save(variableDefinition)
    }

    @Test
    fun get_variable_definition_with_no_value_in_selected_language() {
        val variablesNyNorsk = variableDefinitionService.findByLanguage("nn")
        assertThat(variablesNyNorsk[0].shortName).isEqualTo("test1")
        assertThat(variablesNyNorsk[0].name).isNull()
    }

    @Test
    fun save_variable_definition() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi 2"), (SupportedLanguages.EN to "value 2")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon 2"), (SupportedLanguages.EN to "definition 2")),
            )
        val result = variableDefinitionService.save(variableDefinition)
        assertThat(result.id).isNotNull()
        assertThat(result.id).isNull()
    }
    * */
}

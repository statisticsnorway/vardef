package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import jakarta.inject.Inject
import no.ssb.metadata.models.LanguageStringType
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.services.VariableDefinitionService
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionByIdControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @Nested
    inner class MongoDBDataSetupAndTest {
        private lateinit var variableDefinition: VariableDefinitionDAO
        private lateinit var variableDefinition1: VariableDefinitionDAO
        private lateinit var variableDefinition2: VariableDefinitionDAO
        private lateinit var variables: List<VariableDefinitionDAO>

        @BeforeEach
        fun setUp() {
            variableDefinition =
                VariableDefinitionDAO(
                    id = ObjectId(),
                    definitionId = NanoId.generate(8),
                    name = LanguageStringType(nb = "Transaksjon", nn = null, en = "Transition"),
                    shortName = "test1",
                    definition = LanguageStringType(nb = "definisjon", nn = null, en = "definition"),
                )
            variableDefinition1 =
                VariableDefinitionDAO(
                    id = ObjectId(),
                    definitionId = NanoId.generate(8),
                    name = LanguageStringType(nb = "Bankdør", nn = "Bankdørar", en = "Bank door"),
                    shortName = "bankInngang",
                    definition = LanguageStringType(nb = "Komme inn i banken", nn = "Komme inn i banken", en = "How to get inside a bank"),
                )
            variableDefinition2 =
                VariableDefinitionDAO(
                    id = ObjectId(),
                    definitionId = NanoId.generate(8),
                    name = LanguageStringType(nb = "bilturer", nn = null, en = null),
                    shortName = "bil",
                    definition = LanguageStringType(nb = "Bil som kjøres på turer", nn = null, en = null),
                )
            variables = listOf(variableDefinition, variableDefinition1, variableDefinition2)
            variableDefinitionService.clear()
            for (v in variables) {
                variableDefinitionService.save(v)
            }
        }

        @Test
        fun `get request default language`(spec: RequestSpecification) {
            spec
                .`when`().log().everything()
                .get("/variable-definitions/${variableDefinition.definitionId}")
                .then().log().everything()
                .statusCode(200)
                .body("id", equalTo(variableDefinition.definitionId))
                .body("name", equalTo(variableDefinition.name.nb))
                .body("short_name", equalTo(variableDefinition.shortName))
                .body("definition", equalTo(variableDefinition.definition.nb))
                .header("Content-Language", SupportedLanguages.NB.toString())
        }

        @Test
        fun `get request malformed id`(spec: RequestSpecification) {
            spec
                .`when`().log().everything()
                .get("/variable-definitions/MALFORMED_ID")
                .then().log().everything()
                .statusCode(400)
                .body("_embedded.errors[0].message", containsString("id: must match \"^[a-zA-Z0-9-_]{8}$\""))
        }

        @Test
        fun `get request unknown id`(spec: RequestSpecification) {
            spec
                .`when`().log().everything()
                .get("/variable-definitions/${NanoId.generate(8)}")
                .then().log().everything()
                .statusCode(404)
                .body("_embedded.errors[0].message", containsString("No such variable definition found"))
        }
    }
}

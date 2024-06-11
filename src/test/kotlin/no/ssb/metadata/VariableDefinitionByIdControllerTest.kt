package no.ssb.metadata

import SAVED_VARIABLE_DEFINITION
import SAVED_VARIABLE_DEFINITION_COPY
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

        @BeforeEach
        fun setUp() {
            variableDefinitionService.save(SAVED_VARIABLE_DEFINITION)
            variableDefinitionService.save(SAVED_VARIABLE_DEFINITION_COPY)
        }

        @Test
        fun `get request default language`(spec: RequestSpecification) {
            spec
                .`when`().log().everything()
                .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
                .then().log().everything()
                .statusCode(200)
                .body("id", equalTo(SAVED_VARIABLE_DEFINITION.definitionId))
                .body("name", equalTo(SAVED_VARIABLE_DEFINITION.name.nb))
                .body("short_name", equalTo(SAVED_VARIABLE_DEFINITION.shortName))
                .body("definition", equalTo(SAVED_VARIABLE_DEFINITION.definition.nb))
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

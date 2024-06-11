package no.ssb.metadata

import INPUT_VARIABLE_DEFINITION
import INPUT_VARIABLE_DEFINITION_COPY
import JSON_TEST_INPUT
import SAVED_VARIABLE_DEFINITION
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.services.VariableDefinitionService
import no.ssb.metadata.utils.removeJsonField
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.json.JSONObject


@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionsControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    private val mapper = jacksonObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()
    }

    @Test
    fun `access empty database`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variable-definitions")
            .then()
            .statusCode(200).body("", empty<List<Any>>())
    }

    @Nested
    inner class MongoDBDataSetupAndTest {

        @BeforeEach
        fun setUp() {
            variableDefinitionService.save(INPUT_VARIABLE_DEFINITION.toSavedVariableDefinition())
            variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_COPY.toSavedVariableDefinition())
        }


        @Test
        fun `create variable definition`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(INPUT_VARIABLE_DEFINITION))
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(201)
                .body("short_name", equalTo("landbak"))
                .body("name.nb", equalTo("Landbakgrunn"))
                .body("id", matchesRegex("^[a-zA-Z0-9-_]{8}\$"))
        }

        @Test
        fun `create variable definition with id`(spec: RequestSpecification) {
            val updatedJsonString = JSONObject(JSON_TEST_INPUT).apply {
                put("id", "my-special-id")
            }.toString()
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(updatedJsonString)
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body("_embedded.errors[0].message", containsString("ID may not be specified on creation."))
        }

        @Test
        fun `get request default language`(spec: RequestSpecification) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .get("/variable-definitions")
                .then()
                .statusCode(200)
                .body("[0].definition", equalTo("For personer født"))
                .body("[0].id", notNullValue())
                .header("Content-Language", SupportedLanguages.NB.toString())
        }

        @ParameterizedTest
        @EnumSource(SupportedLanguages::class)
        fun `list variables in supported languages`(
            language: SupportedLanguages,
            spec: RequestSpecification,
        ) {
            val res = spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", language.toString())
                .get("/variable-definitions")
                .then()
                .statusCode(200)
                .body("[1].id", notNullValue())
                .body("[1].name", equalTo(INPUT_VARIABLE_DEFINITION_COPY.name.getValidLanguage(language)))
                .header("Content-Language", language.toString())
        }

        @Test
        fun `get request no value in selected language`(spec: RequestSpecification) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .get("/variable-definitions")
                .then()
                .assertThat().statusCode(200).body("[2]", hasKey("name")).body("[2].name", equalTo(null))
        }

        @Test
        fun `post request incorrect language code`(spec: RequestSpecification) {
            val updatedJsonString = JSONObject(JSON_TEST_INPUT).apply {
                getJSONObject("name").apply {
                    remove("en")
                    put("se", "Landbakgrunn")
                }
            }.toString()
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(updatedJsonString)
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body("_embedded.errors[0].message", containsString("Unknown property [se]"))
        }

        @Test
        fun `post request missing compulsory field`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(removeJsonField(mapper.writeValueAsString(INPUT_VARIABLE_DEFINITION), "name"))
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body("_embedded.errors[0].message", endsWith("null annotate it with @Nullable"))
        }

        @Test
        fun `post request missing compulsory short_name field`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(removeJsonField(mapper.writeValueAsString(INPUT_VARIABLE_DEFINITION), "short_name"))
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body("_embedded.errors[0].message", endsWith("null annotate it with @Nullable"))
        }

        @Test
        fun `get request incorrect language code`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "se")
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body(
                    "_embedded.errors[0].message",
                    startsWith("Failed to convert argument [language] for value [se]"),
                )
        }
    }
}

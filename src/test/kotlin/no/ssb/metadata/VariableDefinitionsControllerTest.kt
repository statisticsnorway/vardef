package no.ssb.metadata

import INPUT_VARIABLE_DEFINITION
import INPUT_VARIABLE_DEFINITION_COPY
import INPUT_VARIABLE_DEFINITION_NO_NAME
import JSON_TEST_INPUT
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.services.VariableDefinitionService
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionsControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

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
            variableDefinitionService.save(INPUT_VARIABLE_DEFINITION_NO_NAME.toSavedVariableDefinition())
        }

        @Test
        fun `create variable definition`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(JSON_TEST_INPUT)
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(HttpStatus.CREATED.code)
                .body("short_name", equalTo("landbak"))
                .body("name.nb", equalTo("Landbakgrunn"))
                .body("id", matchesRegex("^[a-zA-Z0-9-_]{8}\$"))
        }

        @Test
        fun `get request default language`(spec: RequestSpecification) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
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
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", language.toString())
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
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
                .then().log().everything()
                .assertThat().statusCode(HttpStatus.OK.code).body("[2]", hasKey("name")).body("[2].name", equalTo(null))
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

        @ParameterizedTest
        @MethodSource("TestUtils#invalidVariableDefinitions")
        fun `create variable definition with invalid inputs`(
            updatedJsonString: String,
            errorMessage: String,
            spec: RequestSpecification,
        ) {
            spec
                .contentType(ContentType.JSON)
                .body(updatedJsonString)
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body(
                    "_embedded.errors[0].message",
                    containsString(errorMessage),
                )
        }

        @ParameterizedTest
        @MethodSource("TestUtils#variableDefinitionsNonMandatoryFieldsRemoved")
        fun `create variable definition with non mandatory fields removed`(
            updatedJsonString: String,
            spec: RequestSpecification,
        ) {
            spec
                .contentType(ContentType.JSON)
                .body(updatedJsonString)
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(HttpStatus.CREATED.code)
        }

        @ParameterizedTest
        @MethodSource("TestUtils#variableDefinitionsMandatoryFieldsRemoved")
        fun `create variable definition with mandatory fields removed`(
            updatedJsonString: String,
            errorMessage: String,
            spec: RequestSpecification,
        ) {
            spec
                .contentType(ContentType.JSON)
                .body(updatedJsonString).log().body()
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body(
                    "_embedded.errors[0].message",
                    containsString(errorMessage),
                )
        }

        @ParameterizedTest
        @MethodSource("TestUtils#variableDefinitionsVariousVariableStatus")
        fun `test variable status inputs`(
            updatedJsonString: String,
            errorCode: Int,
            spec: RequestSpecification,
        ) {
            spec
                .contentType(ContentType.JSON)
                .body(updatedJsonString).log().body()
                .`when`()
                .post("/variable-definitions")
                .then().log().everything()
                .statusCode(errorCode)
        }
    }
}

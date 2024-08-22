package no.ssb.metadata.vardef

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.INPUT_VARIABLE_DEFINITION_EXAMPLE
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INPUT_VARIABLE_DEFINITION_COPY
import no.ssb.metadata.vardef.utils.JSON_TEST_INPUT
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionsControllerEmptyDatabaseTest {
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
            .statusCode(200)
            .body("", empty<List<Any>>())
    }
}

class VariableDefinitionsControllerTest : BaseVardefTest() {
    @Test
    fun `create variable definition`(spec: RequestSpecification) {
        val startTime = LocalDateTime.now()

        val definitionId =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(JSON_TEST_INPUT)
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(201)
                .body("short_name", equalTo("landbak"))
                .body("name.nb", equalTo("Landbakgrunn"))
                .body("id", matchesRegex("^[a-zA-Z0-9-_]{8}\$"))
                .extract()
                .body()
                .path<String>("id")

        val createdVariableDefinition = variableDefinitionService.getLatestVersionById(definitionId)

        assertThat(createdVariableDefinition.shortName).isEqualTo("landbak")
        assertThat(createdVariableDefinition.createdAt).isCloseTo(startTime, within(1, ChronoUnit.MINUTES))
        assertThat(createdVariableDefinition.createdAt).isEqualTo(createdVariableDefinition.lastUpdatedAt)
    }

    @Test
    fun `create variable definition with no contact information`(spec: RequestSpecification) {
        val updatedJsonString =
            JSONObject(JSON_TEST_INPUT)
                .apply {
                    put("contact", JSONObject.NULL)
                }.toString()
        val definitionId =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(updatedJsonString)
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(201)
                .body("contact", nullValue())
                .extract()
                .body()
                .path<String>("id")

        val createdVariableDefinition = variableDefinitionService.getLatestVersionById(definitionId)

        assertThat(createdVariableDefinition.contact).isNull()
        assertThat(createdVariableDefinition.shortName).isEqualTo("landbak")
    }

    @Test
    fun `check schema example`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(INPUT_VARIABLE_DEFINITION_EXAMPLE)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(201)
    }

    @Test
    fun `create variable definition with id`(spec: RequestSpecification) {
        val updatedJsonString =
            JSONObject(JSON_TEST_INPUT)
                .apply {
                    put("id", "my-special-id")
                }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("_embedded.errors[0].message", containsString("ID may not be specified on creation."))
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
            .header(
                "Content-Language",
                SupportedLanguages.NB
                    .toString(),
            )
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
            .assertThat()
            .statusCode(200)
            .body("[-1]", hasKey("name"))
            .body("[-1].name", equalTo(null))
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
            .then()
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
            .then()
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
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
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
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(errorCode)
    }

    @Test
    fun `get request klass codes`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "nb")
            .get("/variable-definitions")
            .then()
            .assertThat()
            .statusCode(200)
            .body(
                "[0].measurement_type.reference_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/303",
                ),
            ).body("[0].measurement_type.code", equalTo("02.01"))
            .body("[0].measurement_type.title", equalTo("antall"))
            .body(
                "[0].unit_types[0].reference_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/702",
                ),
            ).body("[0].unit_types[0].code", equalTo("01"))
            .body("[0].unit_types[0].title", equalTo("Adresse"))
            .body(
                "[0].subject_fields[0].reference_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/618",
                ),
            ).body("[0].subject_fields[0].code", equalTo("he04"))
            .body("[0].subject_fields[0].title", equalTo("Helsetjenester"))
    }

    @Test
    fun `create variable definition and check klass url`(spec: RequestSpecification) {
        val definitionId =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(JSON_TEST_INPUT)
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .path<String>("id")

        spec
            .given()
            .`when`()
            .get("/variable-definitions/$definitionId")
            .then()
            .body(
                "classification_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/91",
                ),
            )
    }
}

package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.DRAFT_EXAMPLE
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.DRAFT_BUS_EXAMPLE
import no.ssb.metadata.vardef.utils.ERROR_MESSAGE_JSON_PATH
import no.ssb.metadata.vardef.utils.jsonTestInput
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableDefinitionsControllerEmptyDatabaseTest {
    @Inject
    lateinit var variableDefinitionRepository: VariableDefinitionRepository

    @BeforeEach
    fun setUp() {
        variableDefinitionRepository.deleteAll()
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
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put("short_name", "blah")
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
                .body("short_name", equalTo("blah"))
                .body("name.nb", equalTo("Inntektsskatt"))
                .body("id", matchesRegex("^[a-zA-Z0-9-_]{8}\$"))
                .extract()
                .body()
                .path<String>("id")

        val createdVariableDefinition = patches.latest(definitionId)

        assertThat(createdVariableDefinition.shortName).isEqualTo("blah")
        assertThat(createdVariableDefinition.createdAt).isCloseTo(startTime, within(1, ChronoUnit.MINUTES))
        assertThat(createdVariableDefinition.createdAt).isEqualTo(createdVariableDefinition.lastUpdatedAt)
    }

    @Test
    fun `create variable definition with no contact information`(spec: RequestSpecification) {
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put("contact", JSONObject.NULL)
                }.apply {
                    put("short_name", "landbak_copy")
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

        val createdVariableDefinition = patches.latest(definitionId)

        assertThat(createdVariableDefinition.contact).isNull()
        assertThat(createdVariableDefinition.shortName).isEqualTo("landbak_copy")
    }

    @Test
    fun `check schema example`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(DRAFT_EXAMPLE)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(201)
    }

    @Test
    fun `create variable definition with id`(spec: RequestSpecification) {
        val updatedJsonString =
            jsonTestInput()
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
            .body(ERROR_MESSAGE_JSON_PATH, containsString("ID may not be specified on creation."))
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
                ERROR_MESSAGE_JSON_PATH,
                startsWith("Failed to convert argument [language] for value [se]"),
            )
    }

    @Test
    fun `get request default language`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .queryParam("date_of_validity", "2024-01-01")
            .`when`()
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("[0].definition", containsString("Intektsskatt ny definisjon"))
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
            .body("[0].id", notNullValue())
            .body("find { it.short_name == 'bus' }.name", equalTo(DRAFT_BUS_EXAMPLE.name.getValidLanguage(language)))
            .header("Content-Language", language.toString())
    }

    @Test
    fun `get request no value in selected language`(spec: RequestSpecification) {
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put(
                        "short_name",
                        "landbak_copy",
                    )
                    getJSONObject("name").apply {
                        put(
                            "en",
                            JSONObject.NULL,
                        )
                    }
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
                .extract()
                .body()
                .path<String>("id")

        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "en")
            .get("/variable-definitions/$definitionId")
            .then()
            .assertThat()
            .statusCode(200)
            .body("", hasKey("name"))
            .body("name", equalTo(null))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#invalidVariableDefinitions")
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
                ERROR_MESSAGE_JSON_PATH,
                containsString(errorMessage),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#variableDefinitionsNonMandatoryFieldsRemoved")
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
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#variableDefinitionsMandatoryFieldsRemoved")
    fun `create variable definition with mandatory fields removed`(
        updatedJsonString: String,
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
                ERROR_MESSAGE_JSON_PATH,
                containsString("null annotate it with @Nullable"),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#variableDefinitionsVariousVariableStatus")
    fun `create variable definition specify variable status`(
        updatedJsonString: String,
        spec: RequestSpecification,
    ) {
        spec
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
    }

    @Test
    fun `get request klass codes`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "nb")
            .queryParam("date_of_validity", "2024-01-01")
            .`when`()
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
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put("short_name", "landbak_copy")
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

    @ParameterizedTest
    @CsvSource(
        // No definitions are valid on this date
        "1800-01-01, 0",
        // Specific definitions are valid on these dates
        "2021-01-01, 4",
        "2020-01-01, 1",
        // Definitions without a validUntil date defined
        "2024-06-05, 4",
        "3000-12-31, 4",
        // All definitions
        "null, 4",
    )
    fun `filter variable definitions by date`(
        dateOfValidity: String,
        expectedNumber: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .queryParam("date_of_validity", if (dateOfValidity == "null") null else dateOfValidity)
            .`when`()
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("size()", Matchers.equalTo(expectedNumber))
    }

    @Test
    fun `create new variable with existing shortname`(spec: RequestSpecification) {
        val updatedJsonString = jsonTestInput().apply { put("short_name", "intskatt") }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString("already exists."),
            )
    }

    @Test
    fun `all variable definitions has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "nb")
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("find { it }", hasKey("comment"))
            .body("[0].comment", notNullValue())
    }

    @Test
    fun `create new variable with comment field in two languages`(spec: RequestSpecification) {
        val input =
            jsonTestInput()
                .apply {
                    put(
                        "comment",
                        JSONObject().apply {
                            put("nb", "Denne definisjonen trenger tilleggsforklaring")
                            put("en", "This definition needs additional explanation")
                        },
                    )
                    put("short_name", "unique")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(201)
            .body("comment.nb", equalTo("Denne definisjonen trenger tilleggsforklaring"))
            .body("comment.nn", nullValue())
    }

    @Test
    fun `create variable definition returns complete response`(spec: RequestSpecification) {
        val shortName = "blink"
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put("short_name", shortName)
                }.toString()

        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(updatedJsonString)
                .`when`()
                .post("/variable-definitions")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
        assertThat(completeResponse.shortName).isEqualTo(shortName)
    }

    @Test
    fun `create variable definition unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .contentType(ContentType.JSON)
            .body(jsonTestInput())
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(401)
    }
}

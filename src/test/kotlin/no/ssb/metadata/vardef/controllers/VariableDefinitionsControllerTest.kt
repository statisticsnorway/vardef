package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.repositories.VariableDefinitionRepository
import no.ssb.metadata.vardef.utils.*
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
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
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
                .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
                .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("ID may not be specified on creation."))
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
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
                .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .get("/public/variable-definitions/$definitionId")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
                .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .body(jsonTestInput())
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(401)
    }

    @Test
    fun `create variable definition no active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(jsonTestInput())
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @Test
    fun `create variable definition invalid active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(jsonTestInput())
            .queryParam(ACTIVE_GROUP, "invalid-group")
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @ParameterizedTest
    @ValueSource(strings = ["managers", "data-admins"])
    fun `create variable definition active group not developers`(
        groupSuffix: String,
        spec: RequestSpecification,
    ) {
        val group = "play-enhjoern-a-$groupSuffix"
        spec
            .given()
            .auth()
            .oauth2(JwtTokenHelper.jwtTokenSigned(daplaTeams = listOf("play-enhjoern-a"), daplaGroups = listOf(group)).parsedString)
            .contentType(ContentType.JSON)
            .body(jsonTestInput().toString())
            .queryParam(ACTIVE_GROUP, group)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @Test
    fun `list variable definitions return type`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()
        assertThat(jsonMapper.readValue(body, Array<CompleteResponse>::class.java)).isNotNull
    }
}

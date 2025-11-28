package no.ssb.metadata.vardef.controllers.variabledefinitions

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import kotlinx.coroutines.runBlocking
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.matchesRegex
import org.hamcrest.Matchers.nullValue
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class CreateTests : BaseVardefTest() {
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

        val createdVariableDefinition = runBlocking { patches.latest(definitionId) }

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

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
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
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#invalidVariableDefinitions")
    fun `create variable definition with invalid inputs`(
        updatedJsonString: String,
        constraintViolation: Boolean,
        fieldName: String?,
        errorMessage: String?,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(buildProblemJsonResponseSpec(constraintViolation, fieldName, errorMessage))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#variableDefinitionsNonMandatoryFieldsRemoved")
    fun `create variable definition with non mandatory fields removed`(
        updatedJsonString: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.CREATED.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#draftVariableDefinitionMandatoryFieldsRemoved")
    fun `create draft variable definition with mandatory fields removed`(
        updatedJsonString: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#variableDefinitionsVariousVariableStatus")
    fun `create variable definition specify variable status`(
        updatedJsonString: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
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
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "already exists.",
                ),
            )
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
    fun `create variable definition save owner`(spec: RequestSpecification) {
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put("short_name", "ohlala")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(201)
            .body("owner.groups[0]", equalTo(TEST_DEVELOPERS_GROUP))
            .body("owner.groups[1]", nullValue())
            .body("owner.team", equalTo(TEST_TEAM))
    }

    @Test
    fun `create variable definition created by and last updated by is set`(spec: RequestSpecification) {
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
                .extract()
                .body()
                .path<String>("id")

        val createdVariableDefinition = runBlocking { patches.latest(definitionId) }
        assertThat(createdVariableDefinition.createdBy).isEqualTo(TEST_USER)
        assertThat(createdVariableDefinition.lastUpdatedBy).isEqualTo(TEST_USER)
    }

    @Test
    fun `create variable definition no username in token`(spec: RequestSpecification) {
        val updatedJsonString =
            jsonTestInput()
                .apply {
                    put("short_name", "blah")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .auth()
            .oauth2(LabIdTokenHelper.labIdTokenSigned(includeUsername = false).parsedString)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(500)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Internal Server Error: getName(...) must not be null",
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitions.CompanionObject#validUntilInCreateDraft")
    fun `create variable definition valid until in request`(
        input: String,
        httpStatus: HttpStatus,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(httpStatus.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitions.CompanionObject#createDraftMandatoryFields")
    fun `create variable definition missing mandatory fields`(
        input: String,
        httpStatus: HttpStatus,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions")
            .then()
            .statusCode(httpStatus.code)
    }
}

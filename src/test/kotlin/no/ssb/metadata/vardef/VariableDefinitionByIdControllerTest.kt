package no.ssb.metadata.vardef

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.InputVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.JSON_TEST_INPUT
import no.ssb.metadata.vardef.utils.SAVED_DRAFT_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.nullValue
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.temporal.ChronoUnit

class VariableDefinitionByIdControllerTest : BaseVardefTest() {
    @Test
    fun `get request default language`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(200)
            .body("id", equalTo(SAVED_VARIABLE_DEFINITION.definitionId))
            .body("name", equalTo(SAVED_VARIABLE_DEFINITION.name.nb))
            .body("short_name", equalTo(SAVED_VARIABLE_DEFINITION.shortName))
            .body(
                "definition",
                equalTo(
                    variableDefinitionService
                        .getLatestPatchById(
                            SAVED_VARIABLE_DEFINITION.definitionId,
                        ).definition.nb,
                ),
            ).header(
                "Content-Language",
                SupportedLanguages.NB
                    .toString(),
            )
    }

    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("id: must match \"^[a-zA-Z0-9-_]{8}$\""))
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${NanoId.generate(8)}")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @ParameterizedTest
    @CsvSource(
        "1800-01-01, 404, null, null",
        "null, 200, 2021-01-01, null",
        "2021-01-01, 200, 2021-01-01, null",
        "2020-01-01, 200, 1980-12-01, 2020-12-31",
        "2024-06-05, 200, 2021-01-01, null",
        "3000-12-31, 200, 2021-01-01, null",
        nullValues = ["null"],
    )
    fun `get request specific date`(
        dateOfValidity: String?,
        expectedStatusCode: Int,
        expectedValidFrom: String?,
        expectedValidUntil: String?,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .queryParam("date_of_validity", dateOfValidity)
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(expectedStatusCode)
            .body("valid_from", equalTo(expectedValidFrom))
            .body("valid_until", equalTo(expectedValidUntil))
    }

    @Test
    fun `delete request`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(204)
            .header("Content-Type", nullValue())
        assertThat(
            variableDefinitionService
                .listAll()
                .map { it.definitionId },
        ).doesNotContain(SAVED_VARIABLE_DEFINITION.definitionId)
    }

    @Test
    fun `delete request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("id: must match \"^[a-zA-Z0-9-_]{8}$\""))
    }

    @Test
    fun `delete request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${NanoId.generate(8)}")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `update variable definition`(spec: RequestSpecification) {
        val expectedVariableDefinition =
            SAVED_DRAFT_VARIABLE_DEFINITION.copy(
                name = SAVED_DRAFT_VARIABLE_DEFINITION.name.copy(en = "Update"),
            )

        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"name": {
                        "nb": "Inntektsskatt",
                        "nn": "Inntektsskatt",
                        "en": "Update"
                    }}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${expectedVariableDefinition.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()
        val body = jsonMapper.readValue(bodyString, InputVariableDefinition::class.java)

        assertThat(body.id).isEqualTo(SAVED_DRAFT_VARIABLE_DEFINITION.definitionId)
        assertThat(body.name).isEqualTo(expectedVariableDefinition.name)
        assertThat(body.definition).isEqualTo(SAVED_DRAFT_VARIABLE_DEFINITION.definition)

        val updatedVariableDefinition = variableDefinitionService.getLatestPatchById(expectedVariableDefinition.definitionId)
        assertThat(
            updatedVariableDefinition.definitionId,
        ).isEqualTo(expectedVariableDefinition.definitionId)
        assertThat(
            updatedVariableDefinition.createdAt,
        ).isCloseTo(expectedVariableDefinition.createdAt, within(1, ChronoUnit.SECONDS))
        assertThat(
            updatedVariableDefinition.name,
        ).isEqualTo(expectedVariableDefinition.name)
        assertThat(
            updatedVariableDefinition.lastUpdatedAt,
        ).isAfter(expectedVariableDefinition.lastUpdatedAt)
    }

    @Test
    fun `patch variable with published status`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(405)
    }

    @Test
    fun `patch variable with new short name`(spec: RequestSpecification) {
        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name":"hoppebek"}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_VARIABLE_DEFINITION.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()

        val body = jsonMapper.readValue(bodyString, InputVariableDefinition::class.java)
        assertThat(body.shortName).isNotEqualTo(SAVED_DRAFT_VARIABLE_DEFINITION.shortName)
        assertThat(body.id).isEqualTo(SAVED_DRAFT_VARIABLE_DEFINITION.definitionId)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#invalidVariableDefinitions")
    fun `update variable definition with invalid inputs`(
        updatedJsonString: String,
        errorMessage: String,
        spec: RequestSpecification,
    ) {
        spec
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .patch("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                "_embedded.errors[0].message",
                containsString(errorMessage),
            )
    }

    @Test
    fun `patch request malformed id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("id: must match \"^[a-zA-Z0-9-_]{8}$\""))
    }

    @Test
    fun `patch request unknown id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${NanoId.generate(8)}")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `patch request attempt to modify id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"id":  "${NanoId.generate(8)}",
                "name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString("Unknown property [id] encountered during deserialization of type"),
            )
        assertThat(
            variableDefinitionService
                .listAll()
                .map { it.definitionId },
        ).contains(SAVED_VARIABLE_DEFINITION.definitionId)
        assertThat(variableDefinitionService.listAll().map { it.name }).contains(SAVED_VARIABLE_DEFINITION.name)
    }

    @Test
    fun `post not allowed`(spec: RequestSpecification) {
        val input =
            JSONObject(JSON_TEST_INPUT).apply {
                put("short_name", "nothing")
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}")
            .then()
            .statusCode(405)
    }
}

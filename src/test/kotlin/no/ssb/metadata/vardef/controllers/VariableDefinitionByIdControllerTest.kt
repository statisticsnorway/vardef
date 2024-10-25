package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.ACTIVE_TEAM
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.temporal.ChronoUnit

class VariableDefinitionByIdControllerTest : BaseVardefTest() {
    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("must match"))
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${VariableDefinitionService.generateId()}")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
    }

    @ParameterizedTest
    @CsvSource(
        "1800-01-01, 404, null, null",
        "null, 200, 2021-01-01, null",
        "2021-01-01, 200, 2021-01-01, null",
        "2020-01-01, 200, 1980-01-01, 2020-12-31",
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
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(expectedStatusCode)
            .body("valid_from", equalTo(expectedValidFrom))
            .body("valid_until", equalTo(expectedValidUntil))
    }

    @Test
    fun `get request return type`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()
        assertThat(jsonMapper.readValue(body, CompleteResponse::class.java)).isNotNull
    }

    @Test
    fun `delete request draft variable`(spec: RequestSpecification) {
        spec
            .`when`()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM )
            .delete("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(204)
            .header("Content-Type", nullValue())

        assertThat(variableDefinitionService.list().none { it.definitionId == SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId })
    }

    @Test
    fun `delete request published variable`(spec: RequestSpecification) {
        spec
            .`when`()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .delete("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(405)

        assertThat(variableDefinitionService.list().none { it.definitionId == INCOME_TAX_VP1_P1.definitionId })
    }

    @Test
    fun `delete request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .delete("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("must match"))
    }

    @Test
    fun `delete request unknown id`(spec: RequestSpecification) {
        spec
            .given()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .delete("/variable-definitions/${VariableDefinitionService.generateId()}")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
    }

    @Test
    fun `delete request draft variable without active group`(spec: RequestSpecification) {
        spec
            .`when`()
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .delete("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `delete request draft variable invalid active group`(spec: RequestSpecification) {
        spec
            .`when`()
            .queryParam(ACTIVE_GROUP, "invalid group")
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .delete("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `update variable definition`(spec: RequestSpecification) {
        val expected: SavedVariableDefinition =
            SAVED_DRAFT_DEADWEIGHT_EXAMPLE.copy(
                name = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.name.copy(en = "Update"),
            )

        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"name": {
                        "nb": "Dødvekt",
                        "nn": "Dødvekt",
                        "en": "Update"
                    }}
                    """.trimIndent(),
                ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
                .`when`()
                .patch("/variable-definitions/${expected.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()
        val body = jsonMapper.readValue(bodyString, CompleteResponse::class.java)

        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
        assertThat(body.name).isEqualTo(expected.name)
        assertThat(body.definition).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definition)

        val updated: SavedVariableDefinition = patches.latest(expected.definitionId)
        assertThat(updated.definitionId).isEqualTo(expected.definitionId)
        assertThat(updated.createdAt).isCloseTo(expected.createdAt, within(1, ChronoUnit.SECONDS))
        assertThat(updated.name).isEqualTo(expected.name)
        assertThat(updated.lastUpdatedAt).isAfter(expected.lastUpdatedAt)
    }

    @Test
    fun `update variable with published status`(spec: RequestSpecification) {
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(405)
    }

    @Test
    fun `update draft variable definition with new short name`(spec: RequestSpecification) {
        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name":"hoppebek"}
                    """.trimIndent(),
                ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
                .`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()

        val body = jsonMapper.readValue(bodyString, CompleteResponse::class.java)
        assertThat(body.shortName).isNotEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.shortName)
        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
    }

    @Test
    fun `update draft variable definition with another short name that is already in use`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("""{"short_name": "${INCOME_TAX_VP1_P1.shortName}"}""")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.id}")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
            .body(
                "_embedded.errors[0].message",
                containsString("is already in use by another variable definition."),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#invalidVariableDefinitions")
    fun `update draft variable definition with invalid inputs`(
        updatedJsonString: String,
        errorMessage: String,
        spec: RequestSpecification,
    ) {
        spec
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.id}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(errorMessage),
            )
    }

    @Test
    fun `update draft variable definition with malformed id`(spec: RequestSpecification) {
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("must match"))
    }

    @Test
    fun `update draft variable definition with unknown id`(spec: RequestSpecification) {
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${VariableDefinitionService.generateId()}")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
    }

    @Test
    fun `update draft variable definition attempt to modify id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"id":  "${VariableDefinitionService.generateId()}",
                "name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(400)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString("Unknown property [id] encountered during deserialization of type"),
            )
        assertThat(
            variableDefinitionService
                .list()
                .map { it.definitionId },
        ).contains(INCOME_TAX_VP1_P1.definitionId)
        assertThat(variableDefinitionService.list().map { it.name }).contains(INCOME_TAX_VP1_P1.name)
    }

    @Test
    fun `post not allowed`(spec: RequestSpecification) {
        val input =
            jsonTestInput()
                .apply {
                    put("short_name", "nothing")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(405)
    }

    @Test
    fun `variable definition has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(200)
            .body("comment", not(equalTo(null)))
    }

    @Test
    fun `update draft variable definition add comment`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"comment": {
                    "nb": "Legger til merknad",
                    "nn": "Endrer merknad",
                    "en": null
                }}
                """.trimIndent(),
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Legger til merknad"))
            .body("comment.nn", containsString("Endrer merknad"))
            .body("comment.en", nullValue())
    }

    @Test
    fun `update draft variable definition return complete response`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name": "nothing"}
                    """.trimIndent(),
                ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
                .`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .body("", hasKey("owner"))
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
    }

    @Test
    fun `update variable definition invalid active group`(spec: RequestSpecification) {
        val expected: SavedVariableDefinition =
            SAVED_DRAFT_DEADWEIGHT_EXAMPLE.copy(
                name = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.name.copy(en = "Update"),
            )

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Dødvekt",
                    "nn": "Dødvekt",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).queryParam(ACTIVE_GROUP, "invalid-group")
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${expected.definitionId}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `update variable definition no active group`(spec: RequestSpecification) {
        val expected: SavedVariableDefinition =
            SAVED_DRAFT_DEADWEIGHT_EXAMPLE.copy(
                name = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.name.copy(en = "Update"),
            )
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Dødvekt",
                    "nn": "Dødvekt",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .patch("/variable-definitions/${expected.definitionId}")
            .then()
            .statusCode(403)
    }
}

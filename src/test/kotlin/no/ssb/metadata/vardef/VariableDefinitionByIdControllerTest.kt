package no.ssb.metadata.vardef

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.response.ResponseBodyExtractionOptions
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.Draft
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
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
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
            .then()
            .statusCode(200)
            .body("id", equalTo(SAVED_TAX_EXAMPLE.definitionId))
            .body("name", equalTo(SAVED_TAX_EXAMPLE.name.nb))
            .body("short_name", equalTo(SAVED_TAX_EXAMPLE.shortName))
            .body(
                "definition",
                equalTo(
                    variableDefinitionService
                        .getLatestPatchById(
                            SAVED_TAX_EXAMPLE.definitionId,
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
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
            .then()
            .statusCode(expectedStatusCode)
            .body("valid_from", equalTo(expectedValidFrom))
            .body("valid_until", equalTo(expectedValidUntil))
    }

    @Test
    fun `delete request`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
            .then()
            .statusCode(204)
            .header("Content-Type", nullValue())
        assertThat(
            variableDefinitionService
                .listAll()
                .map { it.definitionId },
        ).doesNotContain(SAVED_TAX_EXAMPLE.definitionId)
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
                ).`when`()
                .patch("/variable-definitions/${expectedVariableDefinition.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()
        val body = jsonMapper.readValue(bodyString, Draft::class.java)

        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
        assertThat(body.name).isEqualTo(expectedVariableDefinition.name)
        assertThat(body.definition).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definition)

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
            .patch("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
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
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()

        val body = jsonMapper.readValue(bodyString, Draft::class.java)
        assertThat(body.shortName).isNotEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.shortName)
        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
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
            .`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.id}")
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
            .patch("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
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
        ).contains(SAVED_TAX_EXAMPLE.definitionId)
        assertThat(variableDefinitionService.listAll().map { it.name }).contains(SAVED_TAX_EXAMPLE.name)
    }

    @Test
    fun `post not allowed`(spec: RequestSpecification) {
        val input =
            JSONObject(JSON_TEST_INPUT)
                .apply {
                    put("short_name", "nothing")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
            .then()
            .statusCode(405)
    }

    @Test
    fun `variable definition has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}")
            .then()
            .statusCode(200)
            .body("comment", equalTo(SAVED_TAX_EXAMPLE.comment?.nb))
    }

    @Test
    fun `add comment to draft variable definition`(spec: RequestSpecification) {
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
            ).`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Legger til merknad"))
            .body("comment.nn", containsString("Endrer merknad"))
            .body("comment.en", nullValue())
    }

    @Test
    fun `changes in draft variable definition return complete response`(spec: RequestSpecification) {
        val response: ResponseBodyExtractionOptions? =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name": "nothing"}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .body("", hasKey("owner"))
                .extract()
                .response()

        val jsonResponse = response?.jsonPath()?.getMap<String, Any>("")
        assertThat(jsonResponse?.keys).containsExactlyInAnyOrderElementsOf(ALL_KEYS)
    }
}

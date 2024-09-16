package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class PatchesControllerTest : BaseVardefTest() {
    @Test
    fun `get all patches`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("size()", equalTo(7))
    }

    @Test
    fun `get one patch`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(SAVED_VARIABLE_DEFINITION.definitionId))
            .body("patch_id", equalTo(3))
            .body("short_name", equalTo("landbak"))
    }

    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/MALFORMED_ID/patches")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("must match \"^[a-zA-Z0-9-_]{8}$\""))
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${NanoId.generate(8)}/patches")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `get request unknown patch id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches/8987563")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `delete request`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `patch request`(spec: RequestSpecification) {
        spec
            .`when`()
            .patch("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    @DisplayName("It is not allowed to send in valid from at patches endpoint")
    fun `create new patch valid from in request`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("Valid from is not allowed"))
    }

    @Test
    @DisplayName("It is not possible to edit valid from on patches endpoint")
    fun `create new patch valid from not in request`(spec: RequestSpecification) {
        val testCase =
            JSONObject(JSON_TEST_INPUT)
                .apply {
                    remove("valid_from")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(201)
    }

    @Test
    @DisplayName("Case first patch")
    fun `create new patch when none patches`(spec: RequestSpecification) {
        val testCase =
            JSONObject(JSON_TEST_INPUT)
                .apply {
                    remove("valid_from")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION_COPY.definitionId}/patches")
            .then()
            .statusCode(201)
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
    fun `create new patch with invalid status`(
        variableStatus: VariableStatus,
        spec: RequestSpecification,
    ) {
        val testCase =
            JSONObject(JSON_TEST_INPUT)
                .apply {
                    remove("valid_from")
                }.toString()

        val id =
            variableDefinitionService
                .save(
                    INPUT_VARIABLE_DEFINITION
                        .apply {
                            this.variableStatus = variableStatus
                        }.toSavedVariableDefinition(null),
                ).definitionId

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/$id/patches")
            .then()
            .statusCode(405)
    }
}

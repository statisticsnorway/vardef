package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class PatchesControllerTest : BaseVardefTest() {
    @Test
    fun `get all patches`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("size()", equalTo(NUM_SAVED_TAX_DEFINITIONS))
    }

    @Test
    fun `get one patch`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(SAVED_TAX_EXAMPLE.definitionId))
            .body("patch_id", equalTo(3))
            .body("short_name", equalTo("intskatt"))
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
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches/8987563")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `delete request`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `patch request`(spec: RequestSpecification) {
        spec
            .`when`()
            .patch("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `create new patch`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                    getJSONObject("name").apply {
                        put("nb", "Bybakgrunn")
                    }
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("id", equalTo(SAVED_TAX_EXAMPLE.definitionId))

        val createdPatch = variableDefinitionService.getLatestPatchById(SAVED_TAX_EXAMPLE.definitionId)
        val previousPatch =
            variableDefinitionService.getOnePatchById(
                SAVED_TAX_EXAMPLE.definitionId,
                createdPatch.patchId - 1,
            )

        assertThat(createdPatch.shortName).isEqualTo(previousPatch.shortName)
        assertThat(createdPatch.validFrom).isEqualTo(previousPatch.validFrom)
        assertThat(createdPatch.name.nb).isNotEqualTo(previousPatch.name.nb)
        assertThat(createdPatch.name.en).isEqualTo(previousPatch.name.en)
    }

    @Test
    fun `create new patch valid_from in request`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("short_name")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("valid_from may not be specified here"))
    }

    @Test
    fun `create new patch with valid_until`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                    put("valid_until", "2030-06-30")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(201)
    }

    @Test
    fun `create new patch short_name in request`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("valid_from")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("short_name may not be specified here"))
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
    fun `create new patch with invalid status`(
        variableStatus: VariableStatus,
        spec: RequestSpecification,
    ) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("valid_from")
                    remove("short_name")
                }.toString()

        val id =
            variableDefinitionService
                .save(
                    DRAFT_BUS_EXAMPLE
                        .apply {
                            this.variableStatus = variableStatus
                        }.toSavedVariableDefinition(),
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

    @Test
    fun `list of patches has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("find { it }", hasKey("comment"))
    }

    @Test
    fun `get one patch has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Ny standard for navn til enhetstypeidentifikatorer."))
    }

    @Test
    fun `create new patch with comment`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                    put(
                        "comment",
                        JSONObject().apply {
                            put("en", "This is the reason")
                        },
                    )
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("comment.en", equalTo("This is the reason"))
    }

    @Test
    fun `create new patch return owner information`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(testCase)
            .`when`()
            .post("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("$", hasKey("owner"))
            .body("owner.team", equalTo("pers-skatt"))
            .body("owner.groups[0]", equalTo("pers-skatt-developers"))
    }

    @Test
    fun `get patches return complete response for each variable definition`(spec: RequestSpecification) {
        val responseList =
            spec
                .`when`()
                .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches")
                .then()
                .statusCode(200)
                .body("find { it }", hasKey("owner"))
                .extract().body().`as`(List::class.java);

        /*val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
        val jsonResponse = responseList.asString()
        val jsonAsArrayList: ArrayList<Map<String, *>> = JsonPath.from(jsonResponse).get("")
        assertThat(jsonAsArrayList)
            .allSatisfy { assertThat(it.keys).containsExactlyInAnyOrderElementsOf(ALL_KEYS) }*/
    }

    @Test
    fun `get patch by id return complete response`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions/${SAVED_TAX_EXAMPLE.definitionId}/patches/1")
                .then()
                .statusCode(200)
                .body("$", hasKey("owner"))
                .extract().body().asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
    }
}

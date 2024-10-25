package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

class PatchesControllerTest : BaseVardefTest() {
    companion object {
        fun patchBody(): JSONObject =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                }
    }

    @Test
    fun `get all patches`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("size()", equalTo(numIncomeTaxPatches))
    }

    @Test
    fun `get one patch`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(INCOME_TAX_VP1_P1.definitionId))
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
            .body(ERROR_MESSAGE_JSON_PATH, containsString("must match \"^[a-zA-Z0-9-_]{8}$\""))
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${VariableDefinitionService.generateId()}/patches")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
    }

    @Test
    fun `get request unknown patch id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/8987563")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
    }

    @Test
    fun `delete request`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `patch request`(spec: RequestSpecification) {
        spec
            .`when`()
            .patch("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `create new patch`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                patchBody()
                    .apply {
                        getJSONObject("name").apply {
                            put("nb", "Bybakgrunn")
                        }
                    }.toString(),
            ).queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("id", equalTo(INCOME_TAX_VP1_P1.definitionId))

        val createdPatch = patches.latest(INCOME_TAX_VP1_P1.definitionId)
        val previousPatch =
            patches.get(
                INCOME_TAX_VP2_P6.definitionId,
                INCOME_TAX_VP2_P6.patchId,
            )

        assertThat(createdPatch.shortName).isEqualTo(previousPatch.shortName)
        assertThat(createdPatch.validFrom).isEqualTo(previousPatch.validFrom)
        assertThat(createdPatch.name.nb).isNotEqualTo(previousPatch.name.nb)
        assertThat(createdPatch.name.en).isEqualTo(previousPatch.name.en)
    }

    @Test
    fun `create new patch valid_from in request`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().apply { put("valid_from", "2030-06-30") }.toString())
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(400)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("valid_from may not be specified here"))
    }

    @Test
    fun `create new patch with valid_until`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().apply { put("valid_until", "2030-06-30") }.toString())
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
    }

    @Test
    fun `create new patch short_name in request`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().apply { put("short_name", "vry-shrt-nm") }.toString())
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(400)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("short_name may not be specified here"))
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
    fun `create new patch with invalid status`(
        variableStatus: VariableStatus,
        spec: RequestSpecification,
    ) {
        val id =
            patches
                .create(
                    DRAFT_BUS_EXAMPLE
                        .copy()
                        .apply {
                            this.variableStatus = variableStatus
                        }.toSavedVariableDefinition("play-enhjoern-a-developers"),
                ).definitionId

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().toString())
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/$id/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `list of patches has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("find { it }", hasKey("comment"))
    }

    @Test
    fun `get one patch has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Ny standard for navn til enhetstypeidentifikatorer."))
    }

    @Test
    fun `create new patch with comment`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                patchBody()
                    .apply {
                        put(
                            "comment",
                            JSONObject().apply {
                                put("en", "This is the reason")
                            },
                        )
                    }.toString(),
            ).queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("comment.en", equalTo("This is the reason"))
    }

    @ParameterizedTest
    @CsvSource(
        "1980-01-01, Income tax, Ny standard for navn til enhetstypeidentifikatorer.",
        "2021-01-01, Income tax new definition, Gjelder for færre enhetstyper",
    )
    fun `patch specific validity period`(
        validFrom: String,
        definitionEn: String,
        commentNb: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSONObject().apply { put("classification_reference", "303") }.toString())
            .queryParams("valid_from", validFrom)
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("classification_reference", equalTo("303"))
            .body("definition.en", equalTo(definitionEn))
            .body("comment.nb", equalTo(commentNb))
            .body("patch_id", equalTo(numIncomeTaxPatches + 1))
    }

    @Test
    fun `patch non-existent validity period`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSONObject().apply { put("classification_reference", "303") }.toString())
            .queryParams("valid_from", "3030-12-31")
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No Validity Period with valid_from date"))
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
            .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("$", hasKey("owner"))
            .body("owner.team", equalTo("pers-skatt"))
            .body("owner.groups[0]", equalTo("pers-skatt-developers"))
    }

    @Test
    fun `create new patch return complete response`(spec: RequestSpecification) {
        val testCase =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                }.toString()
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(testCase)
                .queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
                .`when`()
                .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
    }

    @Test
    fun `get patches return complete response for each variable definition`(spec: RequestSpecification) {
        val responseList =
            spec
                .`when`()
                .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
                .then()
                .statusCode(200)
                .body("find { it }", hasKey("owner"))
                .extract()
                .body()
                .asString()

        val completeResponseList = jsonMapper.readValue(responseList, Array<CompleteResponse>::class.java)
        completeResponseList.map { completeResponse ->
            assertThat(completeResponse).isNotNull
        }
    }

    @Test
    fun `get patch by id return complete response`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/1")
                .then()
                .statusCode(200)
                .body("$", hasKey("owner"))
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
    }

    @Test
    fun `create new patch no active group `(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                patchBody()
                    .apply {
                        getJSONObject("name").apply {
                            put("nb", "Bybakgrunn")
                        }
                    }.toString(),
            ).`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @Test
    fun `create new patch invalid active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                patchBody()
                    .apply {
                        getJSONObject("name").apply {
                            put("nb", "Bybakgrunn")
                        }
                    }.toString(),
            ).queryParam(ACTIVE_GROUP, "invalid-group")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @Test
    fun `create new patch incorrect active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                patchBody()
                    .apply {
                        getJSONObject("name").apply {
                            put("nb", "Bybakgrunn")
                        }
                    }.toString(),
            ).queryParam(ACTIVE_GROUP, "play-enhjoern-a-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }
}

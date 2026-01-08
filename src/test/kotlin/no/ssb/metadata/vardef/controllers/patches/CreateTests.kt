package no.ssb.metadata.vardef.controllers.patches

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.controllers.patches.CompanionObject.Companion.patchBody
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class CreateTests : BaseVardefTest() {
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
            ).`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("id", equalTo(INCOME_TAX_VP1_P1.definitionId))
        val createdPatch = patches.latest(INCOME_TAX_VP1_P1.definitionId)
        val previousPatch = patches.get(INCOME_TAX_VP2_P6.definitionId, INCOME_TAX_VP2_P6.patchId)
        assertThat(createdPatch.shortName).isEqualTo(previousPatch.shortName)
        assertThat(createdPatch.validFrom).isEqualTo(previousPatch.validFrom)
        assertThat(createdPatch.name.nb).isNotEqualTo(previousPatch.name.nb)
        assertThat(createdPatch.name.en).isEqualTo(previousPatch.name.en)
    }

    @Test
    fun `create new patch principal not owner`(spec: RequestSpecification) {
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
            ).auth()
            .oauth2(
                LabIdTokenHelper
                    .tokenSigned(
                        activeGroup = "play-enhjoern-b-developers",
                        daplaGroups = listOf("play-enhjoern-b-developers"),
                    ).parsedString,
            ).`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @Test
    fun `create new patch no active group `(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .oauth2(LabIdTokenHelper.tokenSigned(includeActiveGroup = false).parsedString)
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
    fun `create new patch return complete view`(spec: RequestSpecification) {
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
                .`when`()
                .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView).isNotNull
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
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("$", hasKey("owner"))
            .body("owner.team", equalTo("pers-skatt"))
            .body("owner.groups[0]", equalTo("pers-skatt-developers"))
    }

    @Test
    fun `create new patch short_name in request`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().apply { put("short_name", "vry-shrt-nm") }.toString())
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "short_name may not be specified here",
                ),
            )
    }

    @Test
    fun `create new patch valid_from in request`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().apply { put("valid_from", "2030-06-30") }.toString())
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    "valid_from",
                    errorMessage = "may not be specified here",
                ),
            )
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
            ).`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("comment.en", equalTo("This is the reason"))
    }

    @Test
    fun `create new patch on draft variable`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().toString())
            .`when`()
            .post("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `publish internal variable externally`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSONObject("""{"variable_status": "PUBLISHED_EXTERNAL"}""").toString())
            .`when`()
            .post("/variable-definitions/${SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.CREATED.code)
            .body("variable_status", equalTo("PUBLISHED_EXTERNAL"))
    }

    @Test
    fun `create new patch not all languages`(spec: RequestSpecification) {
        val expected: SavedVariableDefinition =
            INCOME_TAX_VP2_P6.copy(
                name = INCOME_TAX_VP2_P6.name.copy(nb = "Update", nn = "Inntektsskatt", en = "Income tax"),
                definition =
                    INCOME_TAX_VP2_P6.definition.copy(
                        en = "Update",
                        nn = "Intektsskatt ny definisjon",
                        nb = "Intektsskatt ny definisjon",
                    ),
                comment =
                    INCOME_TAX_VP2_P6.comment?.copy(
                        nb = "Gjelder for færre enhetstyper",
                        en = null,
                        nn = "Update",
                    ),
            )

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Update"
                },
                "definition": {
                    "en": "Update"
                },
                "comment": {
                    "nn": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("id", equalTo(INCOME_TAX_VP1_P1.definitionId))

        val createdPatch = patches.latest(INCOME_TAX_VP1_P1.definitionId)

        assertThat(createdPatch.name).isEqualTo(expected.name)
        assertThat(createdPatch.definition).isEqualTo(expected.definition)
        assertThat(createdPatch.comment).isEqualTo(expected.comment)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#patchValidUntil")
    fun `create new patch with valid_until`(
        input: String,
        vardefId: String,
        validityPeriod: String?,
        httpStatus: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .apply { validityPeriod?.let { queryParam("valid_from", it) } }
            .`when`()
            .post("/variable-definitions/$vardefId/patches")
            .then()
            .statusCode(httpStatus)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#patchInvalidMandatoryFields")
    fun `attempt to create new patch with mandatory fields`(
        input: String,
        errorMessage: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions/${PATCH_MANDATORY_FIELDS.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    true,
                    null,
                    errorMessage = errorMessage,
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#patchValidMandatoryFields")
    fun `create valid patch with mandatory fields`(
        input: String,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(input)
                .`when`()
                .post("/variable-definitions/${PATCH_MANDATORY_FIELDS.definitionId}/patches")
                .then()
                .statusCode(HttpStatus.CREATED.code)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.patchId).isEqualTo(2)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#internalVariablesMissingLanguages")
    fun `publish variable externally with missing languages`(
        definitionId: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonMapper.writeValueAsString(CreatePatch(variableStatus = VariableStatus.PUBLISHED_EXTERNAL)),
            ).`when`()
            .post("/variable-definitions/$definitionId/patches")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
    }
}

package no.ssb.metadata.vardef.controllers.patches

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.controllers.patches.CompanionObject.Companion.patchBody
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
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
                JwtTokenHelper
                    .jwtTokenSigned(
                        daplaTeams = listOf("play-enhjoern-b"),
                        daplaGroups = listOf("play-enhjoern-b-developers"),
                    ).parsedString,
            ).queryParam(ACTIVE_GROUP, "play-enhjoern-b-developers")
            .`when`()
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
            .statusCode(HttpStatus.UNAUTHORIZED.code)
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
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("comment.en", equalTo("This is the reason"))
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
                        }.toPatch(),
                    DRAFT_BUS_EXAMPLE.definitionId,
                    DRAFT_BUS_EXAMPLE,
                    TEST_USER,
                ).definitionId
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(patchBody().toString())
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/$id/patches")
            .then()
            .statusCode(405)
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
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
        httpStatus: HttpStatus,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .queryParams(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP, "valid_from", validityPeriod)
            .`when`()
            .post("/variable-definitions/$vardefId/patches")
            .then()
            .statusCode(httpStatus.code)
    }
}

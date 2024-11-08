package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

class PatchesControllerTest : BaseVardefTest() {
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
            .statusCode(HttpStatus.NOT_FOUND.code)
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
                        }.toPatch(),
                    DRAFT_BUS_EXAMPLE.definitionId,
                    DRAFT_BUS_EXAMPLE,
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
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .statusCode(HttpStatus.UNAUTHORIZED.code)
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
    fun `get patches unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#definitionIdsAllStatuses")
    fun `get patches authenticated`(
        definitionId: String,
        expectedStatus: String,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("/variable-definitions/$definitionId/patches")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body("[0].variable_status", equalTo(expectedStatus))
    }

    @Test
    fun `get one patch unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/1")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @ParameterizedTest
    @MethodSource("patches")
    fun `get one patch authenticated`(
        patch: SavedVariableDefinition,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/${patch.patchId}")
            .then()
            .statusCode(HttpStatus.OK.code)
    }

    @ParameterizedTest
    @MethodSource("validOwnerUpdates")
    fun `update owner`(
        valueBeforeUpdate: String,
        jsonInput: String,
        jsonPathToActual: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonInput,
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(201)
            .body(jsonPathToActual, not(equalTo(valueBeforeUpdate)))
    }

    @ParameterizedTest
    @MethodSource("invalidOwnerUpdates")
    fun `update owner bad request`(
        jsonInput: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonInput,
            ).queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)

        val savedVariableDefinition =
            variableDefinitionService.getCompleteByDate(
                SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
            )
        assertThat(savedVariableDefinition?.owner?.team).isNotBlank()
        assertThat(savedVariableDefinition?.owner?.groups?.all { it.isNotBlank() })
        assertThat(savedVariableDefinition?.owner?.groups?.isNotEmpty())
    }

    companion object {
        fun patchBody(): JSONObject =
            jsonTestInput()
                .apply {
                    remove("short_name")
                    remove("valid_from")
                }

        @JvmStatic
        fun patches(): List<SavedVariableDefinition> = ALL_INCOME_TAX_PATCHES

        @JvmStatic
        fun validOwnerUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "New team name",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.owner.team,
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-oh-my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-oh-my-team-developers",
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.team",
                ),
                argumentSet(
                    "New group name",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.owner.groups[1],
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-team-developers",
                                            "skip-stat-developers",
                                            "play-foeniks-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.groups[1]",
                ),
                argumentSet(
                    "Add group name",
                    SAVED_INTERNAL_VARIABLE_DEFINITION.owner.groups.last(),
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "my-team-developers",
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                            "play-foeniks-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                    "owner.groups[1]",
                ),
            )

        @JvmStatic
        fun invalidOwnerUpdates(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Team name empty string",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "")
                                    put(
                                        "groups",
                                        listOf(
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                ),
                argumentSet(
                    "Team name null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put(
                                        "groups",
                                        listOf(
                                            "skip-stat-developers",
                                            "play-enhjoern-a-developers",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                ),
                argumentSet(
                    "Groups list is null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                },
                            )
                        }.toString(),
                ),
                argumentSet(
                    "Groups values are null",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put("groups", listOf(null))
                                },
                            )
                        }.toString(),
                ),
                argumentSet(
                    "Groups empty values in list",
                    JSONObject()
                        .apply {
                            put(
                                "owner",
                                JSONObject().apply {
                                    put("team", "my-team")
                                    put(
                                        "groups",
                                        listOf(
                                            "",
                                            "",
                                        ),
                                    )
                                },
                            )
                        }.toString(),
                ),
            )
    }
}

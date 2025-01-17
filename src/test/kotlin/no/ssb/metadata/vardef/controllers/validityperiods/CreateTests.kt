package no.ssb.metadata.vardef.controllers.validityperiods

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.controllers.validityperiods.CompanionObject.Companion.allMandatoryFieldsChanged
import no.ssb.metadata.vardef.controllers.validityperiods.CompanionObject.Companion.noneMandatoryFieldsChanged
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.hasKey
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate

class CreateTests : BaseVardefTest() {
    @Test
    fun `create new validity period incorrect active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(allMandatoryFieldsChanged())
            .auth()
            .oauth2(
                JwtTokenHelper
                    .jwtTokenSigned(
                        daplaTeams = listOf("play-enhjoern-b"),
                        daplaGroups = listOf("play-enhjoern-b-developers"),
                    ).parsedString,
            ).queryParam(ACTIVE_GROUP, "play-enhjoern-b-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @Test
    fun `create new validity period`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(allMandatoryFieldsChanged())
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(201)
        val lastPatchInSecondToLastValidityPeriod =
            validityPeriods
                .getAsMap(INCOME_TAX_VP1_P1.definitionId)
                .let { it.values.elementAt(it.values.size - 2) }
                ?.last()
        val lastPatch = patches.latest(INCOME_TAX_VP1_P1.definitionId)
        assertThat(
            lastPatch.validUntil,
        ).isNull()
        assertThat(
            lastPatchInSecondToLastValidityPeriod?.validUntil,
        ).isEqualTo(lastPatch.validFrom.minusDays(1))
    }

    @Test
    fun `create new validity period before all validity periods`(spec: RequestSpecification) {
        val newValidityPeriodBeforeAll =
            JSONObject(allMandatoryFieldsChanged())
                .apply {
                    put("valid_from", "1923-01-11")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriodBeforeAll)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(201)
        assertThat(
            patches
                .latest(
                    INCOME_TAX_VP1_P1.definitionId,
                ).validUntil,
        ).isEqualTo(LocalDate.of(1979, 12, 31))
    }

    @Test
    fun `create new validity period definition text is not changed`(spec: RequestSpecification) {
        val definitionNotChanged =
            JSONObject(noneMandatoryFieldsChanged())
                .apply {
                    put("valid_from", "2040-01-11")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChanged)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period invalid active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(allMandatoryFieldsChanged())
            .queryParam(ACTIVE_GROUP, "invalid-developers")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(401)
    }

    @Test
    fun `create new validity period no active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(allMandatoryFieldsChanged())
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(403)
    }

    @Test
    fun `create new validity period principal not owner`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(
                JwtTokenHelper
                    .jwtTokenSigned(
                        daplaTeams = listOf("some-other-team"),
                        daplaGroups = listOf("some-other-team-developers"),
                    ).parsedString,
            ).queryParam(ACTIVE_GROUP, "some-other-team-developers")
            .body(allMandatoryFieldsChanged())
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(403)
    }

    @Test
    fun `create new validity period invalid input`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(noneMandatoryFieldsChanged())
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("The date selected cannot be added because it falls between previously added valid dates."))

        val correctValidFrom = JSONObject(noneMandatoryFieldsChanged()).apply { put("valid_from", "2030-01-11") }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(correctValidFrom)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period no valid from`(spec: RequestSpecification) {
        val validFromIsNull =
            JSONObject(allMandatoryFieldsChanged())
                .apply {
                    remove("valid_from")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(validFromIsNull)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Failed to convert argument [newPeriod] for value [null]",
                ),
            )
    }

    @Test
    fun `create new validity period with new short name`(spec: RequestSpecification) {
        val newShortName =
            JSONObject(allMandatoryFieldsChanged())
                .apply {
                    put("short_name", "car")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newShortName)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
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
    fun `create new validity period with valid_until`(spec: RequestSpecification) {
        val newShortName =
            JSONObject(allMandatoryFieldsChanged())
                .apply {
                    put("valid_until", "2030-06-30")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newShortName)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "valid_until may not be specified here",
                ),
            )
    }

    @Test
    fun `create new validity period with draft status`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(allMandatoryFieldsChanged())
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(405)
    }

    @Test
    fun `create new validity period with comment`(spec: RequestSpecification) {
        val addComment =
            JSONObject(allMandatoryFieldsChanged())
                .apply {
                    put(
                        "comment",
                        JSONObject().apply {
                            put("nb", "Vi endrer etter lovverket")
                        },
                    )
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(addComment)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(201)
            .body("$", hasKey("comment"))

        assertThat(
            patches
                .latest(
                    INCOME_TAX_VP1_P1.definitionId,
                ).comment
                ?.nb,
        ).isEqualTo("Vi endrer etter lovverket")
    }

    @Test
    fun `create new validity period invalid valid from`(spec: RequestSpecification) {
        val invalidValidFrom =
            JSONObject(allMandatoryFieldsChanged())
                .apply {
                    put("valid_from", "1990-05-11")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFrom)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                containsString(
                    "The date selected cannot be added because it falls between previously added valid dates.",
                ),
            )
    }

    @Test
    fun `create new validity period missing language`(spec: RequestSpecification) {
        val definitionNotChangedForAll =
            JSONObject(noneMandatoryFieldsChanged())
                .apply {
                    put("valid_from", "2040-01-11")
                    put(
                        "definition",
                        JSONObject().apply {
                            put("nb", "Intektsskatt i økonomi")
                            put("nn", "Intektsskatt i økonomi")
                        },
                    )
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChangedForAll)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
    }

    @Test
    fun `create new validity period return complete response`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(allMandatoryFieldsChanged())
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
        assertThat(completeResponse.lastUpdatedBy).isEqualTo(TEST_USER)
        assertThat(completeResponse.createdBy).isEqualTo("me@example.com")
    }

    @Test
    fun `create validity period during closed validity period`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                JSONObject()
                    .apply {
                        put("valid_from", "2025-01-11")
                        put(
                            "definition",
                            JSONObject().apply {
                                put("nb", "Intektsskatt atter ny definisjon")
                                put("nn", "Intektsskatt atter ny definisjon")
                                put("en", "Yet another definition")
                            },
                        )
                    }.toString(),
            )
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage =
                        "The date selected cannot be added because it falls between previously added valid dates."
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.validityperiods.CompanionObject#newValidityPeriods")
    fun `create new validity period last validity period is closed on one patch`(
        input: String,
        vardefId: String,
        httpStatus: HttpStatus,
        expectedValidFrom: LocalDate?,
        expectedValidUntil: LocalDate?,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(input)
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/variable-definitions/$vardefId/validity-periods")
                .then()
                .statusCode(httpStatus.code)
                .extract()
                .body()
                .asString()

        if (httpStatus == HttpStatus.CREATED) {
            jsonMapper.readValue(body, CompleteResponse::class.java).apply {
                assertThat(this).isNotNull
                assertThat(validFrom).isEqualTo(expectedValidFrom)
                assertThat(validUntil).isEqualTo(expectedValidUntil)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.validityperiods.CompanionObject#testOnlyClosedValidityPeriods")
    fun `new validity on variable period with three closed periods validity periods`(
        input: String,
        httpStatus: Int,
        spec: RequestSpecification
    ) {
        assertThat(SAVED_VARIABLE_INTERNAL_VALIDITY_PERIOD_BEFORE.definitionId).isEqualTo(SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId)
        assertThat(validityPeriods.listComplete(SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId).size).isEqualTo(3)

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(httpStatus)

    }
}

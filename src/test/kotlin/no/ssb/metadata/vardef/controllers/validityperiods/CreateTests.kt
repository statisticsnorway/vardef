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
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CREATED
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
                LabidTokenHelper
                    .labIdTokenSigned(
                        activeGroup = "play-enhjoern-b-developers",
                        daplaGroups = listOf("play-enhjoern-b-developers"),
                    ).parsedString,
            ).`when`()
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
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }



    @Test
    fun `create new validity period no active group`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .oauth2(LabidTokenHelper.labIdTokenSigned(includeActiveGroup = false).parsedString)
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
                LabidTokenHelper
                    .labIdTokenSigned(
                        activeGroup = "some-other-team-developers",
                        daplaGroups = listOf("some-other-team-developers"),
                    ).parsedString,
            ).body(allMandatoryFieldsChanged())
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
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                containsString(
                    "The selected date range cannot be added as it overlaps with or " +
                        "creates a gap in existing validity periods.",
                ),
            )

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
                    "The selected date range cannot be added as it overlaps with or " +
                        "creates a gap in existing validity periods.",
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

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.validityperiods.CompanionObject#newValidityPeriods")
    fun `create new validity period when valid until is set in draft`(
        input: String,
        vardefId: String,
        httpStatus: Int,
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
                .statusCode(httpStatus)
                .extract()
                .body()
                .asString()

        if (httpStatus == HTTP_CREATED) {
            jsonMapper.readValue(body, CompleteResponse::class.java).apply {
                assertThat(this).isNotNull
                assertThat(validFrom).isEqualTo(expectedValidFrom)
                assertThat(validUntil).isEqualTo(expectedValidUntil)
            }
        }
        if (httpStatus == HTTP_BAD_REQUEST) {
            assertThat(body).contains(
                "The selected date range cannot be added as it overlaps with or " +
                    "creates a gap in existing validity periods.",
            )
        }
    }

    @ParameterizedTest
    @MethodSource(
        "no.ssb.metadata.vardef.controllers.validityperiods.CompanionObject#validNewPeriodInvalidManadatoryFields",
    )
    fun `create new validity period invalid mandatory fields`(
        input: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
    }
}

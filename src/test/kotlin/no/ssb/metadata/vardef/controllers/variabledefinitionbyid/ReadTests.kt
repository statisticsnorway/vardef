package no.ssb.metadata.vardef.controllers.variabledefinitionbyid

import io.micronaut.http.HttpStatus
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.PROBLEM_JSON_DETAIL_JSON_PATH
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class ReadTests : BaseVardefTest() {
    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${VariableDefinitionService.generateId()}")
            .then()
            .statusCode(404)
            .body(PROBLEM_JSON_DETAIL_JSON_PATH, containsString("No such variable definition found"))
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

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#definitionIdsAllStatuses")
    fun `get variable definition for all statuses`(
        definitionId: String,
        expectedStatus: String,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("/variable-definitions/$definitionId")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body("variable_status", equalTo(expectedStatus))
    }

    @Test
    fun `get variable definition unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }
}

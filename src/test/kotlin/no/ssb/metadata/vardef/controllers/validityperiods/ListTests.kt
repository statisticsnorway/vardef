package no.ssb.metadata.vardef.controllers.validityperiods

import io.micronaut.http.HttpStatus
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ListTests : BaseVardefTest() {
    @Test
    fun `list validity periods`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(200)
            .body("size()", `is`(2))
            .body("[0].valid_from", equalTo("1980-01-01"))
            .body("[0].patch_id", equalTo(7))
            .body("[1].valid_from", equalTo("2021-01-01"))
            .body("[1].patch_id", equalTo(6))
    }

    @Test
    fun `list validity periods return type`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()
        assertThat(jsonMapper.readValue(body, Array<CompleteResponse>::class.java)).isNotNull
    }

    @Test
    fun `list validity periods unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#definitionIdsAllStatuses")
    fun `list validity periods authenticated`(
        definitionId: String,
        expectedStatus: String,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("/variable-definitions/$definitionId/validity-periods")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body("[0].variable_status", equalTo(expectedStatus))
    }
}

package no.ssb.metadata.vardef.controllers.variabledefinitions

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.NUM_ALL_VARIABLE_DEFINITIONS
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ReadTests : BaseVardefTest() {
    @Test
    fun `klass url renders correctly`(spec: RequestSpecification) {
        spec
            .given()
            .`when`()
            .get("/public/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .body(
                "classification_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/91",
                ),
            )
    }

    @ParameterizedTest
    @CsvSource(
        // No definitions are valid on this date
        "1800-01-01, 0",
        // Specific definitions are valid on these dates
        "2021-01-01, $NUM_ALL_VARIABLE_DEFINITIONS",
        "2020-01-01, 2",
        "2024-06-05, $NUM_ALL_VARIABLE_DEFINITIONS",
        // Definitions without a validUntil date defined
        "3000-12-31, 11",
        // All definitions
        "null, $NUM_ALL_VARIABLE_DEFINITIONS",
    )
    fun `filter variable definitions by date`(
        dateOfValidity: String,
        expectedNumber: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .queryParam("date_of_validity", if (dateOfValidity == "null") null else dateOfValidity)
            .`when`()
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("size()", Matchers.equalTo(expectedNumber))
    }

    @Test
    fun `all variable definitions have comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("find { it }", hasKey("comment"))
            .body("find { it.short_name == 'intskatt' }.comment", notNullValue())
    }

    @Test
    fun `list variable definitions return type`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()
        assertThat(jsonMapper.readValue(body, Array<CompleteResponse>::class.java)).isNotNull
    }

    @Test
    fun `list variables definitions unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @Test
    fun `list variables definitions all statuses`(spec: RequestSpecification) {
        val expectedStatuses =
            setOf(
                VariableStatus.DRAFT,
                VariableStatus.PUBLISHED_INTERNAL,
                VariableStatus.PUBLISHED_EXTERNAL,
            )

        val body =
            spec
                .`when`()
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val variableDefinitions = jsonMapper.readValue(body, Array<CompleteResponse>::class.java)
        val actualStatuses = variableDefinitions.map { it.variableStatus }.toSet()
        assertThat(actualStatuses).containsAll(expectedStatuses)
    }
}

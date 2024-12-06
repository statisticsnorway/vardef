package no.ssb.metadata.vardef.controllers.patches

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource

class UpdateTests : BaseVardefTest() {
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
            .body(PROBLEM_JSON_DETAIL_JSON_PATH, containsString("No Validity Period with valid_from date"))
    }

    @Test
    fun `patch request`(spec: RequestSpecification) {
        spec
            .`when`()
            .patch("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(405)
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

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#validOwnerUpdates")
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
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#invalidOwnerUpdates")
    fun `update owner bad request`(
        jsonInput: String,
        constraintViolation: Boolean,
        expectedErrorMessage: String,
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

        if (constraintViolation) {
            spec.then().body(
                "violations[0].message",
                containsString(expectedErrorMessage),
            )
        } else {
            spec.then().body(
                PROBLEM_JSON_DETAIL_JSON_PATH,
                containsString(expectedErrorMessage),
            )
        }
        val savedVariableDefinition =
            variableDefinitionService.getCompleteByDate(
                SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
            )
        assertThat(savedVariableDefinition?.owner?.team).isNotBlank()
        assertThat(savedVariableDefinition?.owner?.groups?.all { it.isNotBlank() })
        assertThat(savedVariableDefinition?.owner?.groups?.isNotEmpty())
    }
}

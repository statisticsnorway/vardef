package no.ssb.metadata.vardef.controllers.patches

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.MEASUREMENT_TYPE_KLASS_CODE
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
            .body(JSONObject().apply { put("classification_reference", MEASUREMENT_TYPE_KLASS_CODE) }.toString())
            .queryParams("valid_from", "3030-12-31")
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(404)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "No Validity Period with valid_from date",
                ),
            )
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
            .body(JSONObject().apply { put("classification_reference", MEASUREMENT_TYPE_KLASS_CODE) }.toString())
            .queryParams("valid_from", validFrom)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(201)
            .body("classification_reference", equalTo(MEASUREMENT_TYPE_KLASS_CODE))
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
            ).`when`()
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
        fieldName: String?,
        expectedErrorMessage: String?,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonInput,
            ).`when`()
            .post("/variable-definitions/${SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(buildProblemJsonResponseSpec(constraintViolation, null, expectedErrorMessage))

        val savedVariableDefinition =
            variableDefinitionService.getCompleteByDateAndStatus(
                SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId,
            )
        assertThat(savedVariableDefinition?.owner?.team).isNotBlank()
        assertThat(savedVariableDefinition?.owner?.groups?.all { it.isNotBlank() })
        assertThat(savedVariableDefinition?.owner?.groups?.isNotEmpty())
    }

    @Test
    fun `update status to illegal value`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSONObject().apply { put("variable_status", "DRAFT") }.toString())
            .`when`()
            .post("/variable-definitions/${PATCH_MANDATORY_FIELDS.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(buildProblemJsonResponseSpec(false, null, "Changing the status from PUBLISHED_INTERNAL to DRAFT is not allowed."))
    }
}

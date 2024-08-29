package no.ssb.metadata.vardef

import TestUtils
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ValidityPeriodsControllerTest : BaseVardefTest() {
    @Test
    fun `create new validity period not all definitions changed`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
    }

    @Test
    fun `create new validity period all definitions in all languages are changed`(spec: RequestSpecification) {
        val modifiedJson: String = TestUtils.postValidityPeriodOk()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(201)
    }

    @Test
    fun `create new validity period definition text is not changed`(spec: RequestSpecification) {
        val modifiedJson: String = TestUtils.postValidityPeriodDefinitionNotChanged()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period invalid valid from`(spec: RequestSpecification) {
        val modifiedJson: String = TestUtils.postValidityPeriodInvalidValidFrom()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Not valid date"))
    }

    @Test
    fun `create new validity period invalid valid from and not changed definition`(spec: RequestSpecification) {
        val modifiedJson: String = TestUtils.postValidityPeriodInvalidValidFromAndInvalidDefinition()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Not valid date"))
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
    fun `create new validity period with invalid status`(
        variableStatus: VariableStatus,
        spec: RequestSpecification,
    ) {
        var id =
            variableDefinitionService
                .save(
                    INPUT_VARIABLE_DEFINITION
                        .apply {
                            this.variableStatus = variableStatus
                        }.toSavedVariableDefinition(null),
                ).definitionId
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/$id/validity-periods")
            .then()
            .statusCode(405)
            .body(containsString("Only allowed for published variables."))
    }
}

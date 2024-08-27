package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INPUT_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.JSON_TEST_INPUT
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import no.ssb.metadata.vardef.models.VariableStatus

class ValidityPeriodsControllerTest : BaseVardefTest() {

    @Test
    fun `create new validity period`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(201)
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
    fun `create new validity period with invalid status`(
        variableStatus: VariableStatus,
        spec: RequestSpecification
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
            .post("/variable-definitions/${id}/validity-periods")
            .then()
            .statusCode(405)
    }
}

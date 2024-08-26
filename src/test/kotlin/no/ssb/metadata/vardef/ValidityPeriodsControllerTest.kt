package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.JSON_TEST_INPUT
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.junit.jupiter.api.Test

class ValidityPeriodsControllerTest : BaseVardefTest() {
    @Test
    fun `create new validity period`(spec: RequestSpecification) {
        val jsonInput =
            """
            {   
                "valid_from": "2024-06-05",    
            }
            """.trimIndent()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(201)
    }
}

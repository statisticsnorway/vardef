package no.ssb.metadata.vardef.controllers.variabledefinitionbyid

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import no.ssb.metadata.vardef.utils.jsonTestInput
import org.junit.jupiter.api.Test

class CreateTests : BaseVardefTest() {
    @Test
    fun `post not allowed`(spec: RequestSpecification) {
        val input =
            jsonTestInput()
                .apply {
                    put("short_name", "nothing")
                }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .post("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(405)
    }
}

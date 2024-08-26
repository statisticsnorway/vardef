package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.Test
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidityPeriodsControllerTest {

    @Test
    fun `create new validity period`(spec: RequestSpecification) {

        val jsonInput =
            """
        {
            "definition": {
                nb = "For personer født i går",
                nn = "For personer født i går",
                en = "For persons born yesterday",
            },
            "valid_from": "2024-06-05",
        }
        """.trimIndent()
        
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(jsonInput)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(201)

    }
}
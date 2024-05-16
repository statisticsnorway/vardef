package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest {
    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun testVariables(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                "{\"name\":{\"en\":\"Bank connections\",\"nb\":\"Bankforbindelser\",\"nn\":\"value\" },\"shortName\":\"Bank\",\"definition\":{\"en\":\"value\",\"nb\":\"value\",\"nn\":\"value\" }}",
            )
            .`when`()
            .post("/variables")
            .then()
            .statusCode(201)
            .body("shortName", equalTo("Bank"))
            .body("name.nb", equalTo("Bankforbindelser"))
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun testInvalidLanguage(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                "{\"name\":{\"en\":\"Bank connections\",\"nb\":\"Bankforbindelser\",\"no\":\"value\" },\"shortName\":\"Bank\",\"definition\":{\"en\":\"value\",\"nb\":\"value\",\"nn\":\"value\" }}",
            )
            .`when`()
            .post("/variables")
            .then()
            .statusCode(400)
    }

    @Test
    fun testGetVariables(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variables")
            .then()
            .statusCode(200)
    }
}

package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest {
    @Test
    fun testVariables(spec: RequestSpecification) {

        spec
            .given()
            .contentType(ContentType.JSON)
            .body("{\"name\":{\"en\":\"value\",\"nb\":\"value\",\"nn\":\"value\" },\"shortName\":\"value\",\"definition\":{\"en\":\"value\",\"nb\":\"value\",\"nn\":\"value\" }}")
            .`when`()
            .post("/variables")
            .then()
            .statusCode(201)
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

// .body("{\"name\":\"name\",\"shortName\":\"value\",\"definition\":\"value\"}")

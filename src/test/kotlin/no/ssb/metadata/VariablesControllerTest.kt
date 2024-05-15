package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest {
    @Test
    fun testVariables(spec: RequestSpecification) {
        spec
            .`when`()
            .post("/variables")
            .then()
            .statusCode(201)
    }
}
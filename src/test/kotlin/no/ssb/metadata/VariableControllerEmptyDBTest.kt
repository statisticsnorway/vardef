package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableControllerEmptyDBTest {
    @Test
    fun `empty db should return empty list`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variables")
            .then()
            .assertThat().statusCode(200).body("", empty<List<Any>>())
    }
}

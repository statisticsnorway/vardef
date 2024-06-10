package no.ssb.metadata

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test

@MicronautTest
internal class HomeControllerTest {
    @Test
    fun `get on root path redirects to API docs`(spec: RequestSpecification) {
        spec.given().redirects().follow(false)
            .`when`().get("/")
            .then()
            .statusCode(HttpStatus.SEE_OTHER.code)
            .header(HttpHeaders.LOCATION, "/docs/redoc")
    }

    @Test
    fun `home controller not included in API docs`(spec: RequestSpecification) {
        spec.given().get("/swagger/openapi.yaml")
            .then()
            .body(not(containsString("operationId: redirectToDocs")))
    }
}

package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test

internal class HomeControllerTest : BaseVardefTest() {
    @Test
    fun `get on root path redirects to API docs`(spec: RequestSpecification) {
        spec
            .given()
            .redirects()
            .follow(false)
            .`when`()
            .get("/")
            .then()
            .statusCode(HttpStatus.SEE_OTHER.code)
            .header(HttpHeaders.LOCATION, "/swagger-ui")
    }

    @Test
    fun `home controller not included in API docs`(spec: RequestSpecification) {
        spec
            .given()
            .get("/swagger/openapi.yaml")
            .then()
            .body(not(containsString("operationId: redirectToDocs")))
    }
}

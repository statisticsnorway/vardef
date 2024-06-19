package no.ssb.metadata

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.utils.BaseVardefTest
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test

class ManagementEndpointsTest : BaseVardefTest() {
    @Test
    fun `health endpoint`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/health")
            .then()
            .statusCode(200)
    }

    @Test
    fun `metrics endpoint`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/metrics")
            .then()
            .statusCode(200)
    }

    @Test
    fun `swagger docs`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/docs/swagger")
            .then()
            .statusCode(200)
            .body("html.head.title", containsString("variable-definitions"))
    }

    @Test
    fun `redoc docs`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/docs/redoc")
            .then()
            .statusCode(200)
            .body("html.head.title", containsString("variable-definitions"))
    }
}

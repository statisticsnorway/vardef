package no.ssb.metadata.vardef.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@MicronautTest
@Disabled("Doesn't work with random port for management endpoints")
class ManagementEndpointsTest {
    @Property(name = "endpoints.all.port")
    private var endpointsPort: Int = 0

    @Test
    fun `readiness endpoint`(spec: RequestSpecification) {
        spec
            .given()
            .port(endpointsPort)
            .auth()
            .none()
            .`when`()
            .contentType(ContentType.JSON)
            .get("/health/readiness")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
    }

    @Test
    fun `liveness endpoint`(spec: RequestSpecification) {
        spec
            .given()
            .port(endpointsPort)
            .auth()
            .none()
            .`when`()
            .contentType(ContentType.JSON)
            .get("/health/liveness")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
    }

    @Test
    fun `metrics endpoint`(spec: RequestSpecification) {
        spec
            .given()
            .port(endpointsPort)
            .auth()
            .none()
            .`when`()
            .contentType(ContentType.JSON)
            .get("/metrics")
            .then()
            .statusCode(200)
    }

    @Test
    fun `swagger docs`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .contentType(ContentType.JSON)
            .get("/docs/swagger/variable-definitions")
            .then()
            .statusCode(200)
    }
}

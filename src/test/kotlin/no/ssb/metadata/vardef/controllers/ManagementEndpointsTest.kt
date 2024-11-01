package no.ssb.metadata.vardef.controllers

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.Test

@MicronautTest
class ManagementEndpointsTest {
    @Property(name = "endpoints.all.port")
    private var endpointsPort: Int = 0

    @Test
    fun `health endpoint`(spec: RequestSpecification) {
        spec
            .given()
            .port(endpointsPort)
            .auth()
            .none()
            .`when`()
            .contentType(ContentType.JSON)
            .get("/health")
            .then()
            .statusCode(200)
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
            .get("/docs/swagger")
            .then()
            .statusCode(200)
    }

    @Test
    fun `redoc docs`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .contentType(ContentType.JSON)
            .get("/docs/redoc")
            .then()
            .statusCode(200)
    }
}

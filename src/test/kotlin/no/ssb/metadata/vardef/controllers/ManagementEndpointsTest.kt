package no.ssb.metadata.vardef.controllers

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
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
    }

    @Test
    fun `redoc docs`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/docs/redoc")
            .then()
            .statusCode(200)
    }
}

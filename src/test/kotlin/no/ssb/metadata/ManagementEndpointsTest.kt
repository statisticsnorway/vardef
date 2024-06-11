package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.Test

@MicronautTest
class ManagementEndpointsTest {
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
            .then().log().everything()
            .statusCode(200)
    }
}

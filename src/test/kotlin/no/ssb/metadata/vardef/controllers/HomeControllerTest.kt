package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

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
            .header(HttpHeaders.LOCATION, "/docs/swagger/variable-definitions")
    }

    @ParameterizedTest
    @MethodSource("openapiGroupDefinitionFiles")
    fun `home controller not included in API docs`(
        file: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .get("/docs/openapi/variable-definitions/$file.yml")
            .then()
            .statusCode(200)
            .body(not(containsString("operationId: redirectToDocs")))
    }

    companion object {
        @JvmStatic
        fun openapiGroupDefinitionFiles(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Public",
                    "variable-definitions-public",
                ),
                argumentSet(
                    "Internal",
                    "variable-definitions-internal",
                ),
            )
    }
}

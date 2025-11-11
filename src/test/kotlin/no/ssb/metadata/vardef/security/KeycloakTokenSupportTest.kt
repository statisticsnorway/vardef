package no.ssb.metadata.vardef.security

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.http.Method
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@MicronautTest
class KeycloakTokenSupportTest {
    @ParameterizedTest
    @MethodSource("variableCreatorOperations")
    @MethodSource("variableOwnerOperations")
    @MethodSource("variableConsumerOperations")
    fun `request with valid auth`(
        method: Method,
        path: String,
        body: String?,
        spec: RequestSpecification,
    ) {
        if (body != null) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(body)
        }
        spec
            .given()
            .auth()
            .oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .request(method, path)
            .then()
            .statusCode(both(greaterThanOrEqualTo(200)).and(lessThan(300)))
    }

    @ParameterizedTest
    @MethodSource("variableCreatorOperations")
    @MethodSource("variableOwnerOperations")
    fun `request without active group`(
        method: Method,
        path: String,
        body: String?,
        spec: RequestSpecification,
    ) {
        if (body != null) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(body)
        }
        spec
            .given()
            .auth()
            .oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
            .`when`()
            .request(method, path)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @ParameterizedTest
    @MethodSource("variableCreatorOperations")
    @MethodSource("variableOwnerOperations")
    fun `request with invalid active group`(
        method: Method,
        path: String,
        body: String?,
        spec: RequestSpecification,
    ) {
        if (body != null) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(body)
        }
        spec
            .given()
            .auth()
            .oauth2(JwtTokenHelper.jwtTokenSigned().parsedString)
            .queryParam(ACTIVE_GROUP, "invalid-group")
            .`when`()
            .request(method, path)
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @ParameterizedTest
    @MethodSource("variableCreatorOperations")
    fun `create variable definition active group not developers`(
        method: Method,
        path: String,
        body: String?,
        spec: RequestSpecification,
    ) {
        val group = "play-enhjoern-a-managers"
        if (body != null) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(body)
        }
        spec
            .given()
            .auth()
            .oauth2(
                JwtTokenHelper.jwtTokenSigned(daplaGroups = listOf(group)).parsedString,
            ).queryParam(ACTIVE_GROUP, group)
            .`when`()
            .request(method, path)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @ParameterizedTest
    @MethodSource("variableOwnerOperations")
    fun `request where active group is valid but not owner`(
        method: Method,
        path: String,
        body: String?,
        spec: RequestSpecification,
    ) {
        if (body != null) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(body)
        }
        spec
            .given()
            .queryParam(ACTIVE_GROUP, "play-foeniks-a-developers")
            .`when`()
            .request(method, path)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    @ParameterizedTest
    @MethodSource("variableOwnerOperations")
    fun `request with token without required audience claim value`(
        method: Method,
        path: String,
        body: String?,
        spec: RequestSpecification,
    ) {
        if (body != null) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(body)
        }
        spec
            .given()
            .auth()
            .oauth2(JwtTokenHelper.jwtTokenSigned(audienceClaim = listOf("random", "blah")).parsedString)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .request(method, path)
            .then()
            .statusCode(HttpStatus.FORBIDDEN.code)
    }

    companion object {
        /**
         * Tests cases for operations requiring the [VARIABLE_CREATOR] role.
         *
         * All operations annotated with `@Secured(VARIABLE_CREATOR)` should be included here.
         */
        @JvmStatic
        fun variableCreatorOperations(): Stream<Arguments> =
            Stream.of(
                arguments(
                    Method.POST,
                    "/variable-definitions",
                    jsonTestInput().toString(),
                ),
                arguments(
                    Method.POST,
                    "/vardok-migration/2",
                    "",
                ),
            )

        /**
         * Tests cases for operations requiring the [VARIABLE_OWNER] role.
         *
         * All operations annotated with `@Secured(VARIABLE_OWNER)` should be included here.
         */
        @JvmStatic
        fun variableOwnerOperations(): Stream<Arguments> =
            Stream.of(
                arguments(
                    Method.PATCH,
                    "/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}",
                    """
                    {"name": {
                        "nb": "Landbakgrunn",
                        "nn": "Landbakgrunn",
                        "en": "Update"
                    }}
                    """.trimIndent(),
                ),
                arguments(
                    Method.DELETE,
                    "/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}",
                    null,
                ),
                arguments(
                    Method.POST,
                    "/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches",
                    jsonTestInput()
                        .apply {
                            remove("short_name")
                            remove("valid_from")
                        }.toString(),
                ),
                arguments(
                    Method.POST,
                    "/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods",
                    JSONObject()
                        .apply {
                            put("valid_from", "2024-01-11")
                            put(
                                "definition",
                                JSONObject().apply {
                                    put("nb", "Intektsskatt atter ny definisjon")
                                    put("nn", "Intektsskatt atter ny definisjon")
                                    put("en", "Yet another definition")
                                },
                            )
                        }.toString(),
                ),
            )

        /**
         * Tests cases for operations requiring the [VARIABLE_CONSUMER] role.
         *
         * All operations annotated with `@Secured(VARIABLE_CONSUMER)` should be included here.
         * Only operations with the HTTP method GET should have this role.
         */
        @JvmStatic
        fun variableConsumerOperations(): Stream<Arguments> =
            Stream.of(
                arguments(
                    Method.GET,
                    "/variable-definitions",
                    null,
                ),
                arguments(
                    Method.GET,
                    "/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}",
                    null,
                ),
                arguments(
                    Method.GET,
                    "/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches",
                    null,
                ),
                arguments(
                    Method.GET,
                    "/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/${INCOME_TAX_VP1_P1.patchId}",
                    null,
                ),
                arguments(
                    Method.GET,
                    "/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/validity-periods",
                    null,
                ),
            )
    }
}

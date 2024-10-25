package no.ssb.metadata.vardef.controllers

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.ACTIVE_TEAM
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.ERROR_MESSAGE_JSON_PATH
import no.ssb.metadata.vardef.utils.TEST_DEVELOPERS_GROUP
import no.ssb.metadata.vardef.utils.TEST_DEVELOPERS_TEAM
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VarDokMigrationControllerTest : BaseVardefTest() {
    @ParameterizedTest
    @ValueSource(
        ints = [
            2, 948,
        ],
    )
    fun `post request default language`(
        id: Int,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse).isNotNull
    }

    @Test
    fun `post duplicate short name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(201)

        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(409)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Short name wies already exists.",
                ),
            )
    }

    @Test
    fun `duplicate short name`(spec: RequestSpecification) {
        val id = 1607
        spec
            .given()
            .contentType(ContentType.JSON)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .post("/vardok-migration/$id")
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .post("/vardok-migration/$id")
            .then()
            .statusCode(409)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(
                    "already exists",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(ints = [100, 101])
    fun `post request vardok with missing valid date`(
        id: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(
                    "Vardok id $id is missing Valid (valid dates) and can not be saved",
                ),
            )
    }

    @Test
    fun `post request vardok with missing short name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/123")
            .then()
            .statusCode(400)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(
                    "Vardok id 123 is missing DataElementName (short name) and can not be saved",
                ),
            )
    }

    @Test
    fun `post vardok with uppercase data element name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/130")
            .then()
            .statusCode(201)
    }

    @ParameterizedTest
    @ValueSource(ints = [76, 134])
    fun `post vardok with valid but missing valid from date`(
        id: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(
                    "Vardok id $id Valid is missing 'from' date and can not be saved",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            141, 2590,
        ],
    )
    fun `post vardok missing updated statistical unit`(
        id: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(
                    "Vardok id $id StatisticalUnit has outdated unit types and can not be saved",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            16, 3365,
        ],
    )
    fun `vardok dataelement name does not conform to short name rules`(
        id: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                containsString(
                    "varDef.shortName: must match \"^[a-z0-9_]{3,}$\"",
                ),
            )
    }

    @Test
    fun `create vardok unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/948")
            .then()
            .statusCode(401)
    }

    @Test
    fun `create vardok authenticated without active group`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_TEAM, TEST_DEVELOPERS_TEAM)
            .`when`()
            .post("/vardok-migration/948")
            .then()
            .statusCode(403)
    }

    @Test
    fun `post request return owner`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .queryParam(ACTIVE_TEAM, "play-enhjoern-a")
            .`when`()
            .post("/vardok-migration/948")
            .then()
            .statusCode(201)
            .body("owner.groups[0]", equalTo(TEST_DEVELOPERS_GROUP))
            .body("owner.team", equalTo("play-enhjoern-a"))
    }
}

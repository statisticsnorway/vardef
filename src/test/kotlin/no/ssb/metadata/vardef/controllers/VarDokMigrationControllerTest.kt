package no.ssb.metadata.vardef.controllers

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORNAME_KEYWORD
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL
import java.time.LocalDate

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
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(201)

        spec
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(409)
            .spec(buildProblemJsonResponseSpec(false, null, errorMessage = "Short name wies already exists."))
    }

    @ParameterizedTest
    @ValueSource(ints = [100, 101])
    fun `post request vardok with missing valid date`(
        id: Int,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.validFrom).isEqualTo(LocalDate.of(1900, 1, 1))
        assertThat(completeResponse.validUntil).isNull()
    }

    @Test
    fun `post request vardok with missing short name`(spec: RequestSpecification) {
        val definitionId =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/123")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .path<String>("id")

        val createdVariableDefinition = patches.latest(definitionId)
        assertThat(createdVariableDefinition.shortName).startsWith(ILLEGAL_SHORNAME_KEYWORD)
    }

    @Test
    fun `post vardok with uppercase data element name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.validFrom).isEqualTo(LocalDate.of(1900, 1, 1))
        assertThat(completeResponse.validUntil).isNotNull()
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
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Vardok id $id StatisticalUnit has outdated unit types and can not be saved",
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
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .spec(buildProblemJsonResponseSpec(constraintViolation = true, fieldName = "shortName", errorMessage = "must match"))
    }

    @Test
    fun `create vardok return owner`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .post("/vardok-migration/948")
            .then()
            .statusCode(201)
            .body("owner.groups[0]", equalTo(TEST_DEVELOPERS_GROUP))
            .body("owner.team", equalTo(TEST_TEAM))
    }

    @Test
    fun `create vardok has comment`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/566")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.comment?.nb).isNotNull
        assertThat(completeResponse.comment?.en).isNotNull
        assertThat(completeResponse.comment?.nn).isNull()
    }

    @Test
    fun `create vardok has valid until in response`(spec: RequestSpecification) {
        val id = 948
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.validUntil).isEqualTo(LocalDate.of(2001, 12, 31))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.integrations.vardok.VardokResponseTest#mapExternalDocument")
    fun `create vardok externalreference uri`(
        id: Int,
        expectedResult: URL?,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.externalReferenceUri).isEqualTo(expectedResult)
    }
}

package no.ssb.metadata.vardef.controllers.vardokmigration

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORTNAME_KEYWORD
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.TEST_DEVELOPERS_GROUP
import no.ssb.metadata.vardef.utils.TEST_TEAM
import no.ssb.metadata.vardef.utils.buildProblemJsonResponseSpec
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL
import java.time.LocalDate
import kotlin.intArrayOf

class CreateTests:  BaseVardefTest() {
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
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView).isNotNull
        assertThat(completeView.contact.title.nb).contains(GENERATED_CONTACT_KEYWORD)
        assertThat(completeView.contact.email).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `post request duplicate shortname`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/0005")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.shortName).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `post request duplicate shortname when vardok shortname is uppercase`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/0006")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.shortName).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `post request duplicate vardok shortname has uppercase and hyphen`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/0007")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.shortName).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `migrate twice`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(HttpStatus.CREATED.code)

        spec
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Vardok definition with ID 2 already migrated and may not be migrated again.",
                ),
            )
    }

    @Test
    fun `Vardok exception invalid characters`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/0001")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Unexpected character ')'",
                ),
            )
    }

    @Test
    fun `Vardok exception missing fields`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/0002")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage =
                        "Cannot construct instance of " +
                                "`no.ssb.metadata.vardef.integrations.vardok.models.Variable`",
                ),
            )
    }

    @Test
    fun `Vardok creative valid from`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/0003")
                .then()
                .statusCode(HttpStatus.CREATED.code)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.validFrom).isEqualTo(LocalDate.of(+29456, 1, 27))
    }

    @Test
    fun `Vardok exception invalid date`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/0004")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
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
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.validFrom).isEqualTo(LocalDate.of(1900, 1, 1))
        assertThat(completeView.validUntil).isNull()
    }

    @Test
    fun `post request vardok with missing short name`(spec: RequestSpecification) {
        val definitionId =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/123")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .path<String>("id")

        val createdVariableDefinition = patches.latest(definitionId)
        assertThat(createdVariableDefinition.shortName).startsWith(ILLEGAL_SHORTNAME_KEYWORD)
    }

    @Test
    fun `post vardok with uppercase data element name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
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
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.validFrom).isEqualTo(LocalDate.of(1900, 1, 1))
        assertThat(completeView.validUntil).isNotNull()
    }

    @Test
    fun `post vardok external reference`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/1245")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.externalReferenceUri).isNull()
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.vardokmigration.CompanionObject#mapConceptVariableRelations")
    fun `create vardok related variable uris`(
        id: String,
        expectedResult: List<String?>,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.relatedVariableDefinitionUris).isEqualTo(expectedResult)
    }

    @Test
    fun `create vardok return owner`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/948")
            .then()
            .statusCode(201)
            .body("owner.groups[0]", equalTo(TEST_DEVELOPERS_GROUP))
            .body("owner.team", equalTo(TEST_TEAM))
    }

    @Test
    fun `create vardok invalid unit types`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/0000")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Vardok ID 0000: StatisticalUnit is either missing or contains outdated unit types.",
                ),
            )
    }

    @Test
    fun `create vardok has comment`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/566")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.comment?.nb).isNotNull
        assertThat(completeView.comment?.en).isNotNull
        assertThat(completeView.comment?.nn).isNull()
    }

    @Test
    fun `create vardok has valid until in response`(spec: RequestSpecification) {
        val id = 948
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.validUntil).isEqualTo(LocalDate.of(2001, 12, 31))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.vardokmigration.CompanionObject#mapExternalDocument")
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
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.externalReferenceUri).isEqualTo(expectedResult)
    }

    @Test
    fun `create vardok unit types`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/590")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.unitTypes).isEqualTo(listOf("12", "13", "20"))
    }

    @Test
    fun `create vardok with new unit type`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/2194")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.unitTypes).isEqualTo(listOf("29"))
    }

    @Test
    fun `post vardok incorrect updated subject area`(spec: RequestSpecification) {
        val id = 99999
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.subjectFields).isEqualTo(emptyList<String>())
    }



    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.vardokmigration.CompanionObject#newNorwegianUnitTypes")
    fun `create vardok has nn unit type`(
        id: Int,
        expectedUnitType: String,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.unitTypes).isEqualTo(listOf(expectedUnitType))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.vardokmigration.CompanionObject#newNorwegianMultilanguageFields")
    fun `create vardok with nn as primary language`(
        id: Int,
        name: String,
        description: String,
        contactTitle: String,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.name.nn).isEqualTo(name)
        assertThat(completeView.name.nb).isNull()
        assertThat(completeView.definition.nn).isEqualTo(description)
        assertThat(completeView.definition.nb).isNull()
        assertThat(completeView.contact.title.nn).isEqualTo(contactTitle)
        assertThat(completeView.contact.title.nb).isNull()
    }
}
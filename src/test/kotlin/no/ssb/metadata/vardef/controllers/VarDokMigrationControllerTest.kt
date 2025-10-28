package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.constants.GENERATED_CONTACT_KEYWORD
import no.ssb.metadata.vardef.constants.ILLEGAL_SHORTNAME_KEYWORD
import no.ssb.metadata.vardef.integrations.vardok.models.VardokIdResponse
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPairResponse
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.models.CompleteResponse
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.collections.emptyList

class VarDokMigrationControllerTest : BaseVardefTest() {
    @Inject
    lateinit var vardokService: VardokService

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
        assertThat(completeResponse.contact.title.nb).contains(GENERATED_CONTACT_KEYWORD)
        assertThat(completeResponse.contact.email).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `post request duplicate shortname`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/0005")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.shortName).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `post request duplicate shortname when vardok shortname is uppercase`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/0006")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.shortName).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `post request duplicate vardok shortname has uppercase and hyphen`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/0007")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()
        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.shortName).contains(GENERATED_CONTACT_KEYWORD)
    }

    @Test
    fun `migrate twice`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/0003")
                .then()
                .statusCode(HttpStatus.CREATED.code)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.validFrom).isEqualTo(LocalDate.of(+29456, 1, 27))
    }

    @Test
    fun `Vardok exception invalid date`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
        assertThat(createdVariableDefinition.shortName).startsWith(ILLEGAL_SHORTNAME_KEYWORD)
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

    @Test
    fun `post vardok external reference`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/1245")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.externalReferenceUri).isNull()
    }

    @ParameterizedTest
    @MethodSource("mapConceptVariableRelations")
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
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.relatedVariableDefinitionUris).isEqualTo(expectedResult)
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
    fun `create vardok invalid unit types`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
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
    @MethodSource("mapExternalDocument")
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

    @Test
    fun `create vardok unit types`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/590")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.unitTypes).isEqualTo(listOf("12", "13", "20"))
    }

    @Test
    fun `create vardok with new unit type`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/2194")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.unitTypes).isEqualTo(listOf("29"))
    }

    @Test
    fun `post vardok incorrect updated subject area`(spec: RequestSpecification) {
        val id = 99999
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
        assertThat(completeResponse.subjectFields).isEqualTo(emptyList<String>())
    }

    @Test
    fun `vardok id is mapped to vardef id`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/2")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)

        assertThat(vardokService.getVardefIdByVardokId("2")).isEqualTo(completeResponse.id)
    }

    @ParameterizedTest
    @MethodSource("newNorwegianUnitTypes")
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
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.unitTypes).isEqualTo(listOf(expectedUnitType))
    }

    @ParameterizedTest
    @MethodSource("newNorwegianMultilanguageFields")
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
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .post("/vardok-migration/$id")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.name.nn).isEqualTo(name)
        assertThat(completeResponse.name.nb).isNull()
        assertThat(completeResponse.definition.nn).isEqualTo(description)
        assertThat(completeResponse.definition.nb).isNull()
        assertThat(completeResponse.contact.title.nn).isEqualTo(contactTitle)
        assertThat(completeResponse.contact.title.nb).isNull()
    }

    @Test
    fun `get vardef complete response by vardok id`(spec: RequestSpecification) {
        val vardokId = "005"
        val body =
            spec
                .given()
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .get("/vardok-migration/$vardokId")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val completeResponse = jsonMapper.readValue(body, CompleteResponse::class.java)
        assertThat(completeResponse.shortName)
            .isEqualTo("bus")
    }

    @Test
    fun `not migrated vardok id`(spec: RequestSpecification) {
        val vardokId = "555"
        spec
            .given()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .get("/vardok-migration/$vardokId")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `get vardok id by vardef id`(spec: RequestSpecification) {
        val vardokId = "005"
        val definitionId = DRAFT_BUS_EXAMPLE.definitionId
        val body =
            spec
                .given()
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .get("/vardok-migration/$definitionId")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val vardokIdResponse = jsonMapper.readValue(body, VardokIdResponse::class.java)
        assertThat(vardokIdResponse.vardokId).isEqualTo(vardokId)
    }

    @Test
    fun `variable has no mapping to vardef`(spec: RequestSpecification) {
        val definitionId = DRAFT_EXAMPLE_WITH_VALID_UNTIL.definitionId
        spec
            .given()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .get("/vardok-migration/$definitionId")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `get vardef complete response by nonexistent vardef id`(spec: RequestSpecification) {
        val vardefId = "vardefid"
        spec
            .given()
            .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
            .`when`()
            .get("/vardok-migration/$vardefId")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `get vardok vardef mapping`(spec: RequestSpecification) {
        val definitionId = DRAFT_BUS_EXAMPLE.definitionId
        val body =
            spec
                .given()
                .queryParam(ACTIVE_GROUP, TEST_DEVELOPERS_GROUP)
                .`when`()
                .get("/vardok-migration")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val vardokVardefIdPairResponse = jsonMapper.readValue(body, Array<VardokVardefIdPairResponse>::class.java)
        assertThat(vardokVardefIdPairResponse.size).isEqualTo(1)
        assertThat(vardokVardefIdPairResponse[0].vardefId).isEqualTo(definitionId)
    }

    companion object {
        @JvmStatic
        fun newNorwegianUnitTypes(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Verksemd",
                    "2413",
                    "13",
                ),
                argumentSet(
                    "Hushald",
                    "3135",
                    "10",
                ),
            )

        @JvmStatic
        fun newNorwegianMultilanguageFields(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Id 2413",
                    "2413",
                    "Sum utgifter",
                    "Sum av utgifter til løn, innkjøp, refusjon og overføringar.",
                    "${GENERATED_CONTACT_KEYWORD}_tittel",
                ),
                argumentSet(
                    "Id 3135",
                    "3135",
                    "Egenbetaling, barnehagar",
                    "Hushalds utgifter til barnehageplass i kommunale og private barnehagar",
                    "${GENERATED_CONTACT_KEYWORD}_tittel",
                ),
            )

        @JvmStatic
        fun mapExternalDocument(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Vardok id 2 has external document",
                    "2",
                    "http://www.ssb.no/emner/05/90/notat_200372/notat_200372.pdf",
                ),
                argumentSet(
                    "Vardok id 130 has not external document",
                    "130",
                    null,
                ),
                argumentSet(
                    "Vardok id 123 has external document",
                    "123",
                    "http://www.ssb.no/emner/02/01/10/innvbef/om.html",
                ),
                argumentSet(
                    "Vardok id 1245 has invalid external document",
                    "1245",
                    null,
                ),
            )

        @JvmStatic
        fun mapConceptVariableRelations(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Vardok id 2 has several ConceptVariableRelations",
                    "2",
                    listOf(
                        "http://www.ssb.no/conceptvariable/vardok/571",
                        "http://www.ssb.no/conceptvariable/vardok/49",
                        "http://www.ssb.no/conceptvariable/vardok/10",
                        "http://www.ssb.no/conceptvariable/vardok/12",
                        "http://www.ssb.no/conceptvariable/vardok/11",
                    ).map { URI(it).toURL() },
                ),
                argumentSet(
                    "Vardok id 948 has none ConceptVariableRelations",
                    "948",
                    listOf<URL?>(),
                ),
                argumentSet(
                    "Vardok id 1245 has one ConceptVariableRelation",
                    "1245",
                    listOf(
                        "http://www.ssb.no/conceptvariable/vardok/1246",
                    ).map { URI(it).toURL() },
                ),
            )
    }
}

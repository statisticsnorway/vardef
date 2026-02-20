package no.ssb.metadata.vardef.controllers.vardokmigration

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.integrations.vardok.models.VardokIdResponse
import no.ssb.metadata.vardef.integrations.vardok.models.VardokVardefIdPairResponse
import no.ssb.metadata.vardef.integrations.vardok.services.VardokService
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.DRAFT_BUS_EXAMPLE
import no.ssb.metadata.vardef.utils.DRAFT_EXAMPLE_WITH_VALID_UNTIL
import no.ssb.metadata.vardef.utils.buildProblemJsonResponseSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReadTests : BaseVardefTest() {
    @Inject
    lateinit var vardokService: VardokService

    @Test
    fun `vardok id is mapped to vardef id`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body("")
                .`when`()
                .post("/vardok-migration/2")
                .then()
                .statusCode(201)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)

        assertThat(vardokService.getVardefIdByVardokId("2")).isEqualTo(completeView.id)
    }

    @Test
    fun `get vardef complete view by vardok id`(spec: RequestSpecification) {
        val vardokId = "005"
        val body =
            spec
                .given()
                .`when`()
                .get("/vardok-migration/$vardokId")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.shortName)
            .isEqualTo("bus")
    }

    @Test
    fun `get vardok id by vardef id`(spec: RequestSpecification) {
        val vardokId = "005"
        val definitionId = DRAFT_BUS_EXAMPLE.definitionId
        val body =
            spec
                .given()
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
            .`when`()
            .get("/vardok-migration/$definitionId")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `get vardef complete view by nonexistent vardef id`(spec: RequestSpecification) {
        val vardefId = "vardefid"
        spec
            .given()
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

    @Test
    fun `Not migrated vardok id`(spec: RequestSpecification) {
        val vardokId = "555"
        spec
            .given()
            .`when`()
            .get("/vardok-migration/$vardokId")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "No such variable definition found",
                ),
            )
    }

    @Test
    fun `Not migrated vardef id`(spec: RequestSpecification) {
        val definitionId = "Ab12-CD_"
        spec
            .given()
            .`when`()
            .get("/vardok-migration/$definitionId")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "No vardok mapping for vardef id $definitionId",
                ),
            )
    }
}

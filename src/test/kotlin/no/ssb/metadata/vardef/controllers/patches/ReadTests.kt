package no.ssb.metadata.vardef.controllers.patches

import io.micronaut.http.HttpStatus
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.SavedVariableDefinition
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ReadTests : BaseVardefTest() {
    @Test
    fun `get all patches`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("size()", equalTo(numIncomeTaxPatches))
    }

    @Test
    fun `get one patch`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(INCOME_TAX_VP1_P1.definitionId))
            .body("patch_id", equalTo(3))
            .body("short_name", equalTo("intskatt"))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.patches.CompanionObject#patches")
    fun `get one patch authenticated`(
        patch: SavedVariableDefinition,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/${patch.patchId}")
            .then()
            .statusCode(HttpStatus.OK.code)
    }

    @Test
    fun `get one patch has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Ny standard for navn til enhetstypeidentifikatorer."))
    }

    @Test
    fun `get one patch unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/1")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @Test
    fun `get patch by id return complete view`(spec: RequestSpecification) {
        val body =
            spec
                .`when`()
                .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/1")
                .then()
                .statusCode(200)
                .body("$", hasKey("owner"))
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView).isNotNull
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#definitionIdsAllStatuses")
    fun `get patches authenticated`(
        definitionId: String,
        expectedStatus: String,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("/variable-definitions/$definitionId/patches")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body("[0].variable_status", equalTo(expectedStatus))
    }

    @Test
    fun `get patches return complete view for each variable definition`(spec: RequestSpecification) {
        val responseList =
            spec
                .`when`()
                .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
                .then()
                .statusCode(200)
                .body("find { it }", hasKey("owner"))
                .extract()
                .body()
                .asString()

        val completeViewList = jsonMapper.readValue(responseList, Array<CompleteView>::class.java)
        completeViewList.map { completeResponse ->
            assertThat(completeResponse).isNotNull
        }
    }

    @Test
    fun `get patches unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/MALFORMED_ID/patches")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${VariableDefinitionService.generateId()}/patches")
            .then()
            .statusCode(404)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "No such variable definition found",
                ),
            )
    }

    @Test
    fun `get request unknown patch id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches/8987563")
            .then()
            .statusCode(404)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "No such variable definition found",
                ),
            )
    }

    @Test
    fun `list of patches has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("find { it }", hasKey("comment"))
    }
}

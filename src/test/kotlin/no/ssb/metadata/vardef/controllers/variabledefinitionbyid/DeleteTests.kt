package no.ssb.metadata.vardef.controllers.variabledefinitionbyid

import io.micronaut.http.HttpStatus
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.constants.ACTIVE_GROUP
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test

class DeleteTests : BaseVardefTest() {
    @Test
    fun `delete request draft variable`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(204)
            .header("Content-Type", nullValue())

        assertThat(variableDefinitionService.list().none { it.definitionId == SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId })
    }

    @Test
    fun `delete request published variable`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(405)

        assertThat(variableDefinitionService.list().none { it.definitionId == INCOME_TAX_VP1_P1.definitionId })
    }

    @Test
    fun `delete request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `delete request unknown id`(spec: RequestSpecification) {
        spec
            .given()
            .`when`()
            .delete("/variable-definitions/${VariableDefinitionService.generateId()}")
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
    fun `delete request draft variable without active group`(spec: RequestSpecification) {
        spec
            .given()
            .auth().oauth2(LabIdTokenHelper.labIdTokenSigned(includeActiveGroup = false).parsedString)
            .`when`()
            .delete("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(403)
    }

    @Test
    fun `delete request draft variable invalid active group`(spec: RequestSpecification) {
        spec
            .`when`()
            .auth().oauth2(LabIdTokenHelper.labIdTokenSigned(activeGroup = "invalid group").parsedString)
            .delete("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(401)
    }

    @Test
    fun `delete draft variable migrated from vardok`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${DRAFT_BUS_EXAMPLE.definitionId}")
            .then()
            .statusCode(204)
            .header("Content-Type", nullValue())

        assertThat(vardokIdMappingRepository.existsByVardefId(DRAFT_BUS_EXAMPLE.definitionId)).isFalse()
    }
}

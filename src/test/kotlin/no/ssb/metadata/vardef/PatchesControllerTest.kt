package no.ssb.metadata.vardef

import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.SAVED_VARIABLE_DEFINITION
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class PatchesControllerTest : BaseVardefTest() {
    @Test
    fun `get all patches`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(200)
            .body("size()", equalTo(6))
    }

    @Test
    fun `get one patch`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches/3")
            .then()
            .statusCode(200)
            .body("id", equalTo(SAVED_VARIABLE_DEFINITION.definitionId))
            .body("patch_id", equalTo(3))
            .body("short_name", equalTo("landbak"))
    }

    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/MALFORMED_ID/patches")
            .then()
            .statusCode(400)
            .body("_embedded.errors[0].message", containsString("must match \"^[a-zA-Z0-9-_]{8}$\""))
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${NanoId.generate(8)}/patches")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `get request unknown patch id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches/8987563")
            .then()
            .statusCode(404)
            .body("_embedded.errors[0].message", containsString("No such variable definition found"))
    }

    @Test
    fun `delete request`(spec: RequestSpecification) {
        spec
            .`when`()
            .delete("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(405)
    }

    @Test
    fun `patch request`(spec: RequestSpecification) {
        spec
            .`when`()
            .patch("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/patches")
            .then()
            .statusCode(405)
    }
}

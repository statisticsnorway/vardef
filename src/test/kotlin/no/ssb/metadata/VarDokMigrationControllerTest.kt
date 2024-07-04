package no.ssb.metadata

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.junit.jupiter.api.Test

class VarDokMigrationControllerTest : BaseVardefTest() {
    @Test
    fun `get request default language`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/variable-definitions/vardok-migration/2")
            .then()
            .statusCode(201)
    }

    @Test
    fun `get request id with no content`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/variable-definitions/vardok-migration/1")
            .then()
            .statusCode(204)
    }

    // håndtere nullpointerexception....
    @Test
    fun `get request vardok with missing short name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/variable-definitions/vardok-migration/100")
            .then()
            .statusCode(500)
    }
}

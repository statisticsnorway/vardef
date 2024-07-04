package no.ssb.metadata

import SAVED_VARIABLE_DEFINITION
import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.models.InputVariableDefinition
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.utils.BaseVardefTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.temporal.ChronoUnit

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
}

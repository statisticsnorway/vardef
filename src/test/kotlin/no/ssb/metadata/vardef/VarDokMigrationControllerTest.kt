package no.ssb.metadata.vardef

import io.micronaut.context.annotation.Requires
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@Requires(env = ["integration-test"])
class VarDokMigrationControllerTest : BaseVardefTest() {
    @ParameterizedTest
    @ValueSource(
        ints = [
            2, 164, 171, 687, 703, 871, 948,
        ],
    )
    fun `post request default language`(
        id: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(201)
    }

    @Test
    fun `post duplicate short name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(201)

        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Short name 'wies' already exists.",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            1, 4, 14, 23, 218,
        ],
    )
    fun `post request id not found`(
        id: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(404)
    }

    @Test
    fun `duplicate short name`(spec: RequestSpecification) {
        val id = 1607
        spec
            .given()
            .contentType(ContentType.JSON)
            .post("/vardok-migration/$id")
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
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
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
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
            .`when`()
            .post("/vardok-migration/123")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
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
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Vardok id $id Valid is missing 'from' date and can not be saved",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            69, 141, 590, 2157, 2159, 2163, 2217,
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
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Vardok id $id StatisticalUnit has outdated unit types and can not be saved",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            16, 20, 161, 190, 476, 716, 1396, 1660, 2012, 3364, 3365,
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
            .`when`()
            .post("/vardok-migration/$id")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Vardok id $id DataElementName does not conform to short name rules and can not be saved",
                ),
            )
    }
}

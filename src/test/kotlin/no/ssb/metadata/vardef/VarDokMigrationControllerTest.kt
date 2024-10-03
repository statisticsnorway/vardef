package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VarDokMigrationControllerTest : BaseVardefTest() {
    @ParameterizedTest
    @ValueSource(
        ints = [
            2, 164, 171, 173, 177, 683, 687, 703, 730, 746, 752, 754, 755, 756, 753, 871, 935, 936, 937, 948,
            964, 971, 973, 1264, 1265, 1267, 1268, 1282, 1355, 1473, 1487, 1505, 1548, 1549, 1550, 1552,
            1841, 2016, 2318, 2633, 2690, 3007, 3008, 3009, 3010, 3011, 3253, 3256, 3310, 3325, 3448, 3449, 3453,
            3454, 3398, 3251,
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
            69, 141, 590, 1997, 2103, 2124, 2139, 2141, 2142, 2149, 2157, 2159, 2163, 2183, 2194,
            2206, 2216, 2217, 2252, 2590, 2677, 3126, 3394, 3411,
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
            16, 20, 161, 190, 476, 716, 1161, 1162, 1163, 1660,
            2012, 2873, 3057, 3087, 3252, 3364, 3365, 1396,
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

package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.utils.BaseVardefTest
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class VarDokMigrationControllerTest : BaseVardefTest() {
    @Test
    fun `post request default language`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/2")
            .then()
            .statusCode(201)
    }

    @Test
    fun `post request id not found`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/1")
            .then()
            .statusCode(404)
    }

    @Test
    fun `post request vardok with missing valid date`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("")
            .`when`()
            .post("/vardok-migration/100")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Vardok is missing valid dates and can not be saved",
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
                    "Vardok is missing data element name (short name) and can not be saved",
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
    fun `post vardok missing valid from but with valid until`(
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
                    "Vardok is missing valid dates and can not be saved",
                ),
            )
    }

    // nullpointer exception unit types
    @ParameterizedTest
    @ValueSource(ints = [69, 141])
    fun `post vardok missing unitype for statistical`(
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
                    "Variabel is missing valid unit types",
                ),
            )
    }

    @ParameterizedTest
    @ValueSource(
        ints = [
            2, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 18, 25, 26, 27, 28, 30, 33, 42, 49, 50, 51, 52, 88, 89, 90, 91,
            117, 118, 119, 120, 127, 129, 130, 140, 159, 160, 162, 163, 164, 171, 173, 177, 179, 187, 188, 192, 196,
            199, 205, 209, 210, 213, 214, 216, 219, 224, 225, 237, 258, 259, 261, 278, 287, 288, 289, 297, 308, 309,
            310, 311, 312, 313, 315, 316, 329, 341, 350, 360, 361, 396, 397, 400, 401, 419, 420, 421, 423, 429, 431,
            445, 446, 450, 455, 465, 467, 468, 474, 475, 485, 490, 516, 519, 520, 526, 527, 532, 560, 561, 562, 563,
            564, 565, 566, 567, 568, 569, 570, 571, 572, 573, 574, 575, 576, 591, 602, 614, 615, 621, 628, 629,
            630, 631, 642, 644, 646, 647, 650, 653, 654, 655, 656, 657, 662, 666, 667, 668, 669, 670, 671, 672, 679,
            683, 687, 703, 704, 705, 708, 710, 711, 712, 713, 714, 715, 718, 726, 738, 747, 750, 757, 759, 760, 761,
            763, 764, 765, 767, 809, 810, 811, 812, 813, 814, 815, 818, 877, 884, 888, 891, 893, 895, 897, 898,
            899, 900, 901, 902, 903, 904, 905, 906, 907, 908, 909, 910, 911, 912, 924, 925, 926, 927, 928, 929, 930,
            931, 932, 933, 934, 938, 943, 944, 945, 946, 947, 957, 958, 959, 961, 962, 972, 976, 986, 994, 995, 996,
            997, 999, 1006, 1007, 1008, 1009, 1010, 1011, 1022, 1023, 1024, 1056, 1057, 1065, 1072, 1073, 1074,
            1075, 1082, 1100, 1102, 1103, 1104, 1108, 1110, 1165, 1167, 1168, 1170, 1257, 1269, 1270, 1271, 1272,
            1274, 1275, 1278, 1279, 1280, 1281, 1283, 1287, 1292, 1350, 1351, 1359, 1361, 1362, 1378, 1379, 1381,
            1383, 1384, 1385, 1386, 1439, 1441, 1442, 1443, 1449, 1455, 1457, 1460, 1463, 1464, 1465, 1466, 1471,
            1486, 1487, 1505, 1548, 1549, 1550, 1552, 1553, 1554, 1555, 1556, 1559, 1594, 1595,
            1596, 1607, 1613, 1617, 1619, 1620, 1622, 1623, 1624, 1626, 1627, 1628, 1634, 1636, 1637, 1638,
            1661, 1662, 1723, 1755, 1898, 1919, 1985, 1992, 1993, 1995, 1996, 2001, 2018, 2029, 2089, 2093, 2107,
            2122, 2123, 2126, 2127, 2151, 2152, 2160, 2165, 2169, 2171, 2180, 2205, 2227, 2263, 2264, 2268,
            2286, 2287, 2292, 2293, 2365, 2367, 2368, 2369, 2437, 2444, 2445, 2500, 2604, 2634, 2719, 2731,
            2780, 2783, 2845, 2868, 2872, 2874, 2937, 3014, 3045, 3046, 3047, 3048, 3049, 3059, 3060, 3061, 3062,
            3088, 3186, 3207, 3209, 3223, 3248, 3251, 3253, 3256, 3310, 3318, 3319, 3321, 3322, 3323, 3324, 3326,
            3327, 3331, 3332, 3334, 3335, 3336, 3362, 3363, 3366, 3367, 3388, 3391, 3456, 3493, 3494, 3495, 3496,
        ],
    )
    fun `test all`(
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
            16, 20, 69, 161, 190, 476, 590, 716, 730, 746, 752, 753, 754, 755, 756, 871, 935, 936, 937,
            948, 964, 971, 973, 1161, 1162, 1163, 1264, 1265, 1267, 1268, 1282, 1355, 1396, 1473, 1660, 1841, 1997, 2012,
            2016, 2103, 2124, 2139, 2141, 2142, 2149, 2157, 2159, 2163, 2183, 2194, 2206, 2216, 2217, 2252, 2318, 2590,
            2633, 2677, 2690, 2873, 3007, 3008, 3009, 3010, 3011, 3057, 3087, 3126, 3252, 3325, 3364, 3365, 3394, 3398,
            3411, 3448, 3449, 3453, 3454,
        ],
    )
    fun `test failing`(
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
    }
}

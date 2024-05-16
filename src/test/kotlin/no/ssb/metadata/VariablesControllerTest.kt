package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest
    @Inject
    constructor(val vardefService: VariableDefinitionService) {
        // setup tests
        // run testcontainer - with data?
        @Test
        @Suppress("ktlint:standard:max-line-length")
        fun testVariables(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    "{\"name\":{\"en\":\"Bank connections\",\"nb\":\"Bankforbindelser\",\"nn\":\"Bank kamerat\" },\"shortName\":\"Bank\",\"definition\":{\"en\":\"Definition of money\",\"nb\":\"Definisjon av penger\",\"nn\":\"Definere pengar\" }}",
                )
                .`when`()
                .post("/variables")
                .then()
                .statusCode(201)
                .body("shortName", equalTo("Bank"))
                .body("name.nb", equalTo("Bankforbindelser"))
        }

        @Test
        fun testGetAllVariables(spec: RequestSpecification) {
            spec
                .`when`()
                .get("/variables")
                .then()
                .statusCode(200)
        }

        @Test
        fun testGetVariablesReturnList(spec: RequestSpecification) {
            val responseList =
                spec
                    .`when`()
                    .get("/variables")
                    .then()
                    .assertThat()
                    .statusCode(200).extract().body().asString()
            assertThat(responseList).isNotEmpty()
        }

        @Test
        fun getVariablesByLanguage(spec: RequestSpecification) {
            val response =
                spec
            given()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "nb")
                .get("/variables/nb").then().assertThat().statusCode(200)
        }

        @Test
        fun testMethod() {
            val result = vardefService.findByLanguage("nb")
            assertThat(result[0].name).isEqualTo("Bankforbindelser")
            assertThat(result[0].definition).isEqualTo("Definisjon av penger")
        }
    }

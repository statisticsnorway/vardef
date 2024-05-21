package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerGetTest {
    @Test
    fun variablesByNotSupportedLanguageReturnShortName(spec: RequestSpecification) {
        val jsonString =
            """    
            {
                "name": {
                    "en": "Bank door",
                    "nb": "Bankdør",
                    "nn": "Bankdørar"
                },
                "shortName": "bankInngang",
                "definition": {
                    "en": "Get inside the bank",
                    "nb": "Komme inn i banken",
                    "nn": "Komme inn i banken"
                }
            }
            """.trimIndent()
        val postResponse =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(jsonString)
                .`when`()
                .post("/variables")
                .then()
                .statusCode(201).extract().body()
        assertThat(postResponse.toString()).isNotEmpty()

        val getResponse =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "dk")
                .get("/variables")
                .then()
                .assertThat().statusCode(200).and().extract().body().asString()
        assertThat(getResponse).isNotNull().isEqualTo("""[{"shortName":"bankInngang"}]""")
    }
}

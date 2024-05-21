package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest {
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
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "nb")
                .get("/variables/nb")
                .then()
                .assertThat().statusCode(200)
        assertThat(response).isNotNull
    }

    @Test
    fun testGetName() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf(SupportedLanguages.NB to "Norsk navn", SupportedLanguages.EN to "English name"),
                "test",
                mapOf(
                    SupportedLanguages.NB to "definisjon",
                ),
            )
        val resultNorwegian = variableDefinition.getName("nb")
        val resultWithDefaultValue = variableDefinition.getName(null)
        val resultEnglish = variableDefinition.getName("en")
        val nameNorwegian =
            """
            {nb=Norsk navn}
            """.trimIndent()
        val nameEnglish =
            """
            {en=English name}
            """.trimIndent()
        assertThat(resultNorwegian.toString()).isEqualTo(nameNorwegian)
        assertThat(resultWithDefaultValue.toString()).isEqualTo(nameNorwegian)
        assertThat(resultEnglish.toString()).isEqualTo(nameEnglish)
    }

    @Test
    fun testGetDefinition() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf(SupportedLanguages.NB to "Bla bla", SupportedLanguages.EN to "English name"),
                "bla",
                mapOf(
                    SupportedLanguages.EN to "Bank definition",
                    SupportedLanguages.NB to "Bankens rolle i verden",
                ),
            )
        val resultNorwegian = variableDefinition.getDefinition("nb")
        val resultWithDefaultValue = variableDefinition.getDefinition(null)
        val norwegianDefinition =
            """
            {nb=Bankens rolle i verden}
            """.trimIndent()
        assertThat(resultNorwegian.toString()).isEqualTo(norwegianDefinition)
        assertThat(resultWithDefaultValue.toString()).isEqualTo(norwegianDefinition)
    }

    @Test
    fun testHttpRequestsVariables(spec: RequestSpecification) {
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

        val getResponseNorwegianNB =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "nb")
                .get("/variables/nb")
                .then()
                .assertThat().statusCode(200).extract().body().asString()
        assertThat((getResponseNorwegianNB)).isNotNull()

        val getResponseNorwegianNN =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "nn")
                .get("/variables/nn")
                .then()
                .assertThat().statusCode(200).extract().body().asString()
        assertThat((getResponseNorwegianNN)).isNotNull()

        val getResponseEnglish =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .get("/variables/en")
                .then()
                .assertThat().statusCode(200).extract().body().asString()
        assertThat((getResponseEnglish)).isNotNull()
    }
}

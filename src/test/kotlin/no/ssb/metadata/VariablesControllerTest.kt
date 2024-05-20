package no.ssb.metadata

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest
    @Inject
    constructor(val vardefService: VariableDefinitionService) {


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
        fun testGetVariablesByLanguage() {
            val result = vardefService.findByLanguage("nb")
            val name = """
                {nb=Bankforbindelser}
            """.trimIndent()
            val definition = """
                {nb=Definisjon av penger}
            """.trimIndent()
            assertThat(result[0].name.toString()).isEqualTo(name)
            assertThat(result[0].definition.toString()).isEqualTo(definition)
        }

        @Test
        fun testGetName() {
            val variableDefinition = VariableDefinitionDAO(null, mapOf(SupportedLanguages.NB to "Bla bla", SupportedLanguages.EN to "English name"),"bla", mapOf(
                SupportedLanguages.NB to "nnnn"))
            val resultNorwegian = vardefService.getName(variableDefinition,"nb")
            val resultEnglish = vardefService.getName(variableDefinition,"en")
            val nameNorwegian = """
                {nb=Bla bla}
            """.trimIndent()
            val nameEnglish = """
                {en=English name}
            """.trimIndent()
            assertThat(resultNorwegian.toString()).isEqualTo(nameNorwegian)
            assertThat(resultEnglish.toString()).isEqualTo(nameEnglish)
        }

        @Test
        fun testGetDefinition() {
            val variableDefinition = VariableDefinitionDAO(null, mapOf(SupportedLanguages.NB to "Bla bla", SupportedLanguages.EN to "English name"),"bla", mapOf(
                SupportedLanguages.EN to "Bank definition", SupportedLanguages.NB to "Bankens rolle i verden"))
            val result = vardefService.getDefinition(variableDefinition,"nb")
            val definition = """
                {nb=Bankens rolle i verden}
            """.trimIndent()
            assertThat(result.toString()).isEqualTo(definition)
        }

        @Test
        fun testHttpRequestsVariables(spec: RequestSpecification) {
            val postResponse =
                spec
                    .given()
                    .contentType(ContentType.JSON)
                    .body(
                        "{\"name\":{\"en\":\"Bank door\",\"nb\":\"Bankdør\",\"nn\":\"Bankdørar\" },\"shortName\":\"bankInngang\",\"definition\":{\"en\":\"Get inside the bank\",\"nb\":\"Komme inn i banken\",\"nn\":\"Komme inn i banken\" }}",
                    )
                    .`when`()
                    .post("/variables")
                    .then()
                    .statusCode(201)
            assertThat(postResponse.toString()).isNotEmpty()

            val getResponseNorwegianBokmaal =
                spec
                    .`when`()
                    .contentType(ContentType.JSON)
                    .header("Accept-Language", "nb")
                    .get("/variables/nb")
                    .then()
                    .assertThat().statusCode(200).extract().body().asString()
            assertThat((getResponseNorwegianBokmaal)).isNotNull()

            val getResponseNorwegianNynorsk =
                spec
                    .`when`()
                    .contentType(ContentType.JSON)
                    .header("Accept-Language", "nn")
                    .get("/variables/nn")
                    .then()
                    .assertThat().statusCode(200).extract().body().asString()
            assertThat((getResponseNorwegianNynorsk)).isNotNull()

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

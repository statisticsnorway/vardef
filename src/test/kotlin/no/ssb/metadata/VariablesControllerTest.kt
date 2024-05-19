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
        fun testMethod() {
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
            val result = vardefService.getName(variableDefinition,"nb")
            val name = """
                {nb=Bla bla}
            """.trimIndent()
            assertThat(result.toString()).isEqualTo(name)
        }

        @Test
        fun testGetResult() {
            val variableDefinition = VariableDefinitionDAO(null, mapOf(SupportedLanguages.NB to "Bla bla", SupportedLanguages.EN to "English name"),"bla", mapOf(
                SupportedLanguages.EN to "Bank definition", SupportedLanguages.NB to "Bankens rolle i verden"))
            val result = vardefService.getDefinition(variableDefinition,"nb")
            val definition = """
                {nb=Bankens rolle i verden}
            """.trimIndent()
            assertThat(result.toString()).isEqualTo(definition)
        }
    }

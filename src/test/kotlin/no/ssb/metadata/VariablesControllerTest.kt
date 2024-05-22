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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@MicronautTest
class VariablesControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @BeforeEach
    fun setUp() {
        val variableDefinition =
            VariableDefinitionDAO(
                null,
                mapOf((SupportedLanguages.NB to "verdi"), (SupportedLanguages.EN to "value")),
                "test1",
                mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinitionService.save(variableDefinition)
    }

    @Test
    fun testTest() {
        val all = variableDefinitionService.findAll()
        val variables = variableDefinitionService.findByLanguage("nb")
        assertThat(variables[0].shortName).isEqualTo("test1")
        assertThat(all[0].shortName).isEqualTo("test1")
    }

    @Test
    fun testVariables(spec: RequestSpecification) {
        val jsonString =
            """    
            {
                "name": {
                    "en": "Bank connections",
                    "nb": "Bankforbindelser",
                    "nn": "Bankavtale"
                },
                "short_name": "bank",
                "definition": {
                    "en": "definition of money",
                    "nb": "definisjon av penger",
                    "nn": "pengers verdi"
                }
            }
            """.trimIndent()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(jsonString)
            .`when`()
            .post("/variables")
            .then()
            .statusCode(201)
            .body("short_name", equalTo("bank"))
            .body("name.nb", equalTo("Bankforbindelser"))
    }

    @Test
    fun getVariablesByLanguage(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "nb")
            .get("/variables")
            .then()
            .assertThat().statusCode(200)
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
                "short_name": "bankInngang",
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

        val getResponseNorwegianNN =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "nn")
                .get("/variables")
                .then()
                .assertThat().statusCode(200).extract().body().asString()

        assertThat((getResponseNorwegianNN)).isNotNull()
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun testHttpRequestNorwegianVariables(spec: RequestSpecification) {
        val jsonString =
            """    
            {
                "name": {
                    "en": "Bank door",
                    "nb": "Bankdør",
                    "nn": "Bankdørar"
                },
                "short_name": "bankInngang",
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
                .get("/variables")
                .then()
                .assertThat().statusCode(200).extract().body().asString()
        assertThat((getResponseNorwegianNB)).isNotNull()

        val getResponseDefaultLanguage =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .get("/variables")
                .then()
                .assertThat().statusCode(200).extract().body().asString()
        assertThat(getResponseDefaultLanguage).isNotEmpty()
    }

    @Test
    fun testHttpRequestEnglishVariables(spec: RequestSpecification) {
        val jsonString =
            """    
            {
                "name": {
                    "en": "Bank door",
                    "nb": "Bankdør",
                    "nn": "Bankdørar"
                },
                "short_name": "bankInngang",
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

        val getResponseEnglish =
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .get("/variables")
                .then()
                .assertThat().statusCode(200).extract().body().asPrettyString()
        assertThat((getResponseEnglish)).isNotNull()
    }

    @Test
    fun testIncorrectLanguageCode(spec: RequestSpecification) {
        val jsonString =
            """    
            {
                "name": {
                    "en": "Bank connections",
                    "nb": "Bankforbindelser",
                    "se": "Bankavtale"
                },
                "short_name": "bank",
                "definition": {
                    "en": "definition of money",
                    "nb": "definisjon av penger",
                    "nn": "pengers verdi"
                }
            }
            """.trimIndent()
        val getResponseIncorrectLanguage =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(jsonString)
                .`when`()
                .post("/variables")
                .then()
                .assertThat().statusCode(400).extract().body().asPrettyString()
        assertThat((getResponseIncorrectLanguage) == "Unknown language code se. Valid values are [nb, nn, en")
    }
}

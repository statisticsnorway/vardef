package no.ssb.metadata

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanugages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.endsWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    private lateinit var variableDefinition: VariableDefinitionDAO
    private lateinit var variableDefinition1: VariableDefinitionDAO
    private lateinit var variableDefinition2: VariableDefinitionDAO

    @BeforeEach
    fun setUp() {
        variableDefinition =
            VariableDefinitionDAO(
                null,
                SupportedLanugages(nb="Transaksjon", nn=null, en="Transition"),

                //mapOf((SupportedLanguages.NB to "Transaksjon"), (SupportedLanguages.EN to "Transition")),
                "test1",
                SupportedLanugages(nb="definisjon", nn=null, en="definition"),
                //mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
            )
        variableDefinition1 =
            VariableDefinitionDAO(
                null,
                SupportedLanugages(nb="Bankdør", nn="Bankdørar", en="Bank door"),

//                mapOf(
//                    (SupportedLanguages.NB to "Bankdør"),
//                    (SupportedLanguages.EN to "Bank door"),
//                    (SupportedLanguages.NN to "Bankdørar"),
//                ),
                "bankInngang",
                SupportedLanugages(nb="Komme inn i banken", nn="Komme inn i banken", en="How to get inside a bank"),

//                mapOf(
//                    (SupportedLanguages.NB to "Komme inn i banken"),
//                    (SupportedLanguages.EN to "How to get inside a bank"),
//                    (SupportedLanguages.NN to "Komme inn i banken"),
//                ),
            )
        variableDefinition2 =
            VariableDefinitionDAO(
                null,
                SupportedLanugages(nb="bilturer", nn=null, en=null),
                //mapOf(SupportedLanguages.NB to "bilturer"),
                "bil",
                SupportedLanugages(nb="Bil som kjøres på turer", nn=null, en=null),
                //mapOf(SupportedLanguages.NB to "Bil som kjøres på turer"),
            )
        val variables = listOf<VariableDefinitionDAO>(variableDefinition, variableDefinition1, variableDefinition2)
        for (v in variables) {
            variableDefinitionService.save(v)
        }
    }

    @Test
    fun testPostVariableDefinition(spec: RequestSpecification) {
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
    fun testGetVariableDefinitions(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variables")
            .then()
            .assertThat().statusCode(200)
    }

    @Test
    fun testGetVariableDefinitionsByLanguageNN(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "nn")
            .get("/variables")
            .then()
            .assertThat().statusCode(200).body("[1].name", equalTo("Bankdørar"))
    }

    @Test
    fun testGetVariableDefinitionsByLanguageNB(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "nb")
            .get("/variables")
            .then()
            .assertThat().statusCode(200).body("[0].name", equalTo("Transaksjon"))
    }

    @Test
    fun testGetVariableDefinitionsByDefaultLanguage(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variables")
            .then()
            .assertThat().statusCode(200).body("[0].definition", equalTo("definisjon"))
    }

    @Test
    fun testGetVariableDefinitionsByLanguageEN(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "en")
            .get("/variables")
            .then()
            .assertThat().statusCode(200).body("[1].name", equalTo("Bank door"))
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
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(jsonString)
            .`when`()
            .post("/variables")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("_embedded.errors[0].message", equalTo("Unknown language code se. Valid values are [nb, nn, en]"))
    }

    @Test
    fun testMissingCompulsoryField(spec: RequestSpecification) {
        val jsonString =
            """
            {
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
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body("_embedded.errors[0].message", endsWith("must not be empty"))
    }

    @Test
    fun testIncorrectLanguageCodeGet(spec: RequestSpecification) {
        val getResponseIncorrectLanguage =
            spec
                .given()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "se")
                .get("/variables")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body("_embedded.errors[0].message", equalTo("Unknown language code se. Valid values are [nb, nn, en]"))
    }
}

package no.ssb.metadata

import SIMPLE_VARIABLE_DEFINITION
import VARIABLE_JSON_INPUT
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.SavedVariableDefinition
import no.ssb.metadata.services.VariableDefinitionService
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariablesControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @Test
    fun `access empty database`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variables")
            .then()
            .statusCode(200).body("", empty<List<Any>>())
    }

    @Nested
    inner class MongoDBDataSetupAndTest {

        @BeforeEach
        fun setUp() {
            variableDefinitionService.save(SIMPLE_VARIABLE_DEFINITION)
        }
//            variableDefinition =
//                SavedVariableDefinition(
//                    null,
//                    LanguageStringType(nb = "Transaksjon", nn = null, en = "Transition"),
//                    "test1",
//                    LanguageStringType(nb = "definisjon", nn = null, en = "definition"),
//                )
//            variableDefinition1 =
//                SavedVariableDefinition(
//                    null,
//                    LanguageStringType(nb = "Bankdør", nn = "Bankdørar", en = "Bank door"),
//                    "bankInngang",
//                    LanguageStringType(nb = "Komme inn i banken", nn = "Komme inn i banken", en = "How to get inside a bank"),
//                )
//            variableDefinition2 =
//                SavedVariableDefinition(
//                    null,
//                    LanguageStringType(nb = "bilturer", nn = null, en = null),
//                    "bil",
//                    LanguageStringType(nb = "Bil som kjøres på turer", nn = null, en = null),
//                )
//            variables = listOf<SavedVariableDefinition>(variableDefinition)//, variableDefinition1, variableDefinition2)
//            for (v in variables) {
//                variableDefinitionService.save(v)
//            }
//        }
//
        @Test
        fun `post request new variable definition`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(VARIABLE_JSON_INPUT)
                .`when`()
                .post("/variables")
                .then().log().body()
                .statusCode(201)
                .body("short_name", equalTo("landbak"))
                .body("name.nb", equalTo("Landbakgrunn"))
        }

        @Test
        fun `get request default language`(spec: RequestSpecification) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .get("/variables")
                .then()
                .statusCode(200)
                .body("[0].definition", equalTo("For personer født"))
                .body("[0].id", notNullValue())
                .header("Content-Language", SupportedLanguages.NB.toString())
        }
//
//        @ParameterizedTest
//        @EnumSource(SupportedLanguages::class)
//        fun `list variables in supported languages`(
//            language: SupportedLanguages,
//            spec: RequestSpecification,
//        ) {
//            val res = spec
//                .`when`()
//                .contentType(ContentType.JSON)
//                .header("Accept-Language", language.toString())
//                .get("/variables")
//                .then()
//                .statusCode(200)
//
//                //.body("[1].id", notNullValue())
//                //.body("[1].name", equalTo(SIMPLE_VARIABLE_DEFINITION.name.nb))
//                //.header("Content-Language", language.toString())
//            //println(res.toString())
//        }
//
//        @Test
//        fun `get request no value in selected language`(spec: RequestSpecification) {
//            spec
//                .`when`()
//                .contentType(ContentType.JSON)
//                .header("Accept-Language", "en")
//                .get("/variables")
//                .then()
//                .assertThat().statusCode(200).body("[2]", hasKey("name")).body("[2].name", equalTo(null))
//        }

//        @Test
//        fun `post request incorrect language code`(spec: RequestSpecification) {
//            val jsonString =
//                """
//                {
//                    "name": {
//                        "en": "Bank connections",
//                        "nb": "Bankforbindelser",
//                        "se": "Bankavtale"
//                    },
//                    "short_name": "bank",
//                    "definition": {
//                        "en": "definition of money",
//                        "nb": "definisjon av penger",
//                        "nn": "pengers verdi"
//                    }
//                }
//                """.trimIndent()
//            spec
//                .given()
//                .contentType(ContentType.JSON)
//                .body(jsonString)
//                .`when`()
//                .post("/variables")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.code)
//                .body("_embedded.errors[0].message", containsString("Unknown property [se]"))
//        }
//
//        @Test
//        fun `post request missing compulsory field`(spec: RequestSpecification) {
//            val jsonString =
//                """
//                {
//                    "short_name": "bank",
//                    "definition": {
//                        "en": "definition of money",
//                        "nb": "definisjon av penger",
//                        "nn": "pengers verdi"
//                    }
//                }
//                """.trimIndent()
//            spec
//                .given()
//                .contentType(ContentType.JSON)
//                .body(jsonString)
//                .`when`()
//                .post("/variables")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.code)
//                .body("_embedded.errors[0].message", endsWith("null annotate it with @Nullable"))
//        }
//
//        @Test
//        fun `post request missing compulsory short_name field`(spec: RequestSpecification) {
//            val jsonString =
//                """
//                {
//                    "name": {
//                        "en": "Bank connections",
//                        "nb": "Bankforbindelser",
//                        "nn": "Bankavtale"
//                    },
//                    "definition": {
//                        "en": "definition of money",
//                        "nb": "definisjon av penger",
//                        "nn": "pengers verdi"
//                    }
//                }
//                """.trimIndent()
//            spec
//                .given()
//                .contentType(ContentType.JSON)
//                .body(jsonString)
//                .`when`()
//                .post("/variables")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.code)
//                .body("_embedded.errors[0].message", endsWith("null annotate it with @Nullable"))
//        }
//
//        @Test
//        fun `get request incorrect language code`(spec: RequestSpecification) {
//            spec
//                .given()
//                .contentType(ContentType.JSON)
//                .header("Accept-Language", "se")
//                .get("/variables")
//                .then()
//                .statusCode(HttpStatus.BAD_REQUEST.code)
//                .body(
//                    "_embedded.errors[0].message",
//                    startsWith("Failed to convert argument [language] for value [se]"),
//                )
//        }
    }
}

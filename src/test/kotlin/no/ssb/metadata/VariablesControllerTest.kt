package no.ssb.metadata

import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.models.VariableDefinitionDAO
import no.ssb.metadata.services.VariableDefinitionService
import org.assertj.core.api.Assertions
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
        private lateinit var variableDefinition: VariableDefinitionDAO
        private lateinit var variableDefinition1: VariableDefinitionDAO
        private lateinit var variableDefinition2: VariableDefinitionDAO
        private lateinit var variables: List<VariableDefinitionDAO>

        @BeforeEach
        fun setUp() {
            variableDefinition =
                VariableDefinitionDAO(
                    null,
                    mapOf((SupportedLanguages.NB to "Transaksjon"), (SupportedLanguages.EN to "Transition")),
                    "test1",
                    mapOf((SupportedLanguages.NB to "definisjon"), (SupportedLanguages.EN to "definition")),
                )
            variableDefinition1 =
                VariableDefinitionDAO(
                    null,
                    mapOf(
                        (SupportedLanguages.NB to "Bankdør"),
                        (SupportedLanguages.EN to "Bank door"),
                        (SupportedLanguages.NN to "Bankdørar"),
                    ),
                    "bankInngang",
                    mapOf(
                        (SupportedLanguages.NB to "Komme inn i banken"),
                        (SupportedLanguages.EN to "How to get inside a bank"),
                        (SupportedLanguages.NN to "Komme inn i banken"),
                    ),
                )
            variableDefinition2 =
                VariableDefinitionDAO(
                    null,
                    mapOf(SupportedLanguages.NB to "bilturer"),
                    "bil",
                    mapOf(SupportedLanguages.NB to "Bil som kjøres på turer"),
                )
            variables = listOf<VariableDefinitionDAO>(variableDefinition, variableDefinition1, variableDefinition2)
            for (v in variables) {
                variableDefinitionService.save(v)
            }
        }

        @Test
        fun `post request new variable definition`(spec: RequestSpecification) {
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
        fun `get request default language`(spec: RequestSpecification) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .get("/variables")
                .then()
                .statusCode(200)
                .body("[0].definition", equalTo("definisjon"))
                .body("id", notNullValue())
                .header("Content-Language", SupportedLanguages.NB.toString())
        }

        @ParameterizedTest
        @EnumSource(SupportedLanguages::class)
        fun `list variables in supported languages`(
            language: SupportedLanguages,
            spec: RequestSpecification,
        ) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", language.toString())
                .get("/variables")
                .then()
                .statusCode(200)
                .body("[1].name", equalTo(variableDefinition1.name[language]))
                .body("id", notNullValue())
                .header("Content-Language", language.toString())
        }

        @Test
        fun `get request no value in selected language`(spec: RequestSpecification) {
            spec
                .`when`()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "en")
                .get("/variables")
                .then()
                .assertThat().statusCode(200).body("[2].name", nullValue())
        }

        @Test
        fun `post request incorrect language code`(spec: RequestSpecification) {
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
        fun `post request missing compulsory field`(spec: RequestSpecification) {
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
        fun `get request incorrect language code`(spec: RequestSpecification) {
            spec
                .given()
                .contentType(ContentType.JSON)
                .header("Accept-Language", "se")
                .get("/variables")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.code)
                .body(
                    "_embedded.errors[0].message",
                    equalTo("Unknown language code se. Valid values are [nb, nn, en]"),
                )
        }

        @Test
        fun `varDef id is only created once`()  {
            val idBeforeSave = variableDefinition1.id
            val shortNameBeforeSave = variableDefinition1.shortName
            variableDefinition1.shortName = "bankUtgang"
            val result = variableDefinitionService.save(variableDefinition1)
            Assertions.assertThat(idBeforeSave).isSameAs(result.id)
            Assertions.assertThat(shortNameBeforeSave).isNotSameAs(result.shortName)
        }

        @Test
        fun `all variables has mongodb id`()  {
            val variableDefinition3 =
                VariableDefinitionDAO(
                    null,
                    mapOf(SupportedLanguages.NB to "bilturer"),
                    "bil",
                    mapOf(SupportedLanguages.NB to "Bil som kjøres på turer"),
                )
            val result = variableDefinitionService.save(variableDefinition3)
            Assertions.assertThat(result.objectId).isNotNull()
            variableDefinition3.shortName = "campingbil"
            val result2 = variableDefinitionService.save(variableDefinition3)
            Assertions.assertThat(result2.objectId).isNotSameAs(result.objectId)
            Assertions.assertThat(result2.id).isSameAs(result.id)
        }
    }
}

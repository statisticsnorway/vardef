package no.ssb.metadata.vardef.controllers.variabledefinitions

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ReadTests : BaseVardefTest() {
    @Test
    fun `klass url renders correctly`(spec: RequestSpecification) {
        spec
            .given()
            .`when`()
            .get("/public/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .body(
                "classification_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/91",
                ),
            )
    }

    @ParameterizedTest
    @CsvSource(
        // No definitions are valid on this date
        "1800-01-01, 0",
        // Specific definitions are valid on these dates
        "2021-01-01, 9",
        "2020-01-01, 2",
        "2024-06-05, 12",
        // Definitions without a validUntil date defined
        "3000-12-31, 7",
        // All definitions
        "null, $NUM_ALL_VARIABLE_DEFINITIONS",
    )
    fun `filter variable definitions by date`(
        dateOfValidity: String,
        expectedNumber: Int,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .queryParam("date_of_validity", if (dateOfValidity == "null") null else dateOfValidity)
            .`when`()
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("size()", Matchers.equalTo(expectedNumber))
    }

    @Test
    fun `all variable definitions have comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .get("/variable-definitions")
            .then()
            .statusCode(200)
            .body("find { it }", hasKey("comment"))
            .body("find { it.short_name == 'intskatt' }.comment", notNullValue())
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#returnFormatsArrays")
    fun <T> `list variable definitions return type`(
        render: Boolean?,
        expectedClass: Class<Array<T>>,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .`when`()
                .queryParam("render", render)
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()
        val variableDefinitions: Array<T> = jsonMapper.readValue(body, expectedClass) as Array<T>

        assertThat(variableDefinitions.size).isEqualTo(NUM_ALL_VARIABLE_DEFINITIONS)
        assertThat(variableDefinitions[0]).isInstanceOf(expectedClass.componentType)
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `list variable definitions in supported languages`(
        language: SupportedLanguages,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, language.toString())
                .queryParam("render", true)
                .`when`()
                .get("/variable-definitions")
                .then()
                .statusCode(200)
                .header("Content-Language", language.toString())
                .extract()
                .body()
                .asString()

        assertThat(jsonMapper.readValue(body, Array<RenderedVariableDefinition>::class.java)).isNotNull
    }

    @Test
    fun `list variables definitions unauthenticated`(spec: RequestSpecification) {
        spec
            .given()
            .auth()
            .none()
            .`when`()
            .get("/variable-definitions")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.code)
    }

    @Test
    fun `list variables definitions all statuses`(spec: RequestSpecification) {
        val expectedStatuses =
            setOf(
                VariableStatus.DRAFT,
                VariableStatus.PUBLISHED_INTERNAL,
                VariableStatus.PUBLISHED_EXTERNAL,
            )

        val body =
            spec
                .`when`()
                .get("/variable-definitions")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val variableDefinitions = jsonMapper.readValue(body, Array<CompleteView>::class.java)
        val actualStatuses = variableDefinitions.map { it.variableStatus }.toSet()
        assertThat(actualStatuses).containsAll(expectedStatuses)
    }

    @Test
    fun `get variable definition by short name that does not exist`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions?short_name=nonexistent_shortname")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body("", empty<List<Any>>())
    }

    @Test
    fun `get variable definition by similar short names`(spec: RequestSpecification) {
        variableDefinitionRepository.save(DRAFT_BUS_EXAMPLE.copy(shortName = "bussrute"))
        val body =
            spec
                .`when`()
                .get("/variable-definitions?short_name=${DRAFT_BUS_EXAMPLE.shortName}")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val variableDefinitions = jsonMapper.readValue(body, Array<CompleteView>::class.java)
        assertThat(variableDefinitions.size).isEqualTo(1)
        assertThat(variableDefinitions[0].shortName).isEqualTo(DRAFT_BUS_EXAMPLE.shortName)
        assertThat(variableDefinitions[0].patchId).isEqualTo(1)
    }

    @ParameterizedTest
    @MethodSource("getByShortnames")
    fun `get variable definition by short name with or without date`(
        url: String,
        expectedPatchId: Int,
        spec: RequestSpecification,
    ) {
        val body =
            spec
                .`when`()
                .get(url)
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val variableDefinitions = jsonMapper.readValue(body, Array<CompleteView>::class.java)
        assertThat(variableDefinitions[0].patchId).isEqualTo(expectedPatchId)
    }

    companion object {
        @JvmStatic
        fun getByShortnames(): Stream<Arguments> =
            Stream.of(
                argumentSet(
                    "Short name and no date",
                    "/variable-definitions?short_name=${INCOME_TAX_VP2_P6.shortName}",
                    6,
                ),
                argumentSet(
                    "Shortname and date",
                    "/variable-definitions?date_of_validity=1990-01-01&short_name=${INCOME_TAX_VP2_P6.shortName}",
                    7,
                ),
                argumentSet(
                    "No patches",
                    "/variable-definitions?short_name=${DRAFT_BUS_EXAMPLE.shortName}",
                    1,
                ),
            )
    }
}

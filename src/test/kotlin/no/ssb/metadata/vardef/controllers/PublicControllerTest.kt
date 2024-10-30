package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.RenderedVariableDefinition
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.*
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

class PublicControllerTest : BaseVardefTest() {
    init {
        RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
        // Disable auth since we don't expect authentication on public endpoints
        RestAssured.authentication = RestAssured.DEFAULT_AUTH
    }

    private val publicVariableDefinitionsPath = "/public/variable-definitions"

    @Test
    fun `list variable definitions incorrect language code`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "se")
            .get(publicVariableDefinitionsPath)
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .body(
                ERROR_MESSAGE_JSON_PATH,
                startsWith("Failed to convert argument [language] for value [se]"),
            )
    }

    @Test
    fun `list variable definitions default language`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .queryParam("date_of_validity", "2024-01-01")
                .`when`()
                .get(publicVariableDefinitionsPath)
                .then()
                .statusCode(200)
                .body("[0].definition", containsString("Intektsskatt ny definisjon"))
                .body("[0].id", notNullValue())
                .header(
                    "Content-Language",
                    SupportedLanguages.NB
                        .toString(),
                ).extract()
                .body()
                .asString()

        jsonMapper.readValue(body, Array<RenderedVariableDefinition>::class.java).forEach {
            assertThat(validityPeriods.getLatestPatchInLastValidityPeriod(it.id).variableStatus)
                .isEqualTo(VariableStatus.PUBLISHED_EXTERNAL)
        }
    }

    @ParameterizedTest
    @EnumSource(SupportedLanguages::class)
    fun `list variable definitions in supported languages`(
        language: SupportedLanguages,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, language.toString())
            .get(publicVariableDefinitionsPath)
            .then()
            .statusCode(200)
            .body("[0].id", notNullValue())
            .body("find { it.short_name == 'intskatt' }.name", equalTo(INCOME_TAX_VP2_P6.name.getValidLanguage(language)))
            .header("Content-Language", language.toString())
    }

    @Test
    fun `get variable definition default language`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("$publicVariableDefinitionsPath/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(200)
            .body("id", equalTo(INCOME_TAX_VP1_P1.definitionId))
            .body("name", equalTo(INCOME_TAX_VP1_P1.name.nb))
            .body("short_name", equalTo(INCOME_TAX_VP1_P1.shortName))
            .body(
                "definition",
                equalTo(
                    validityPeriods
                        .getLatestPatchInLastValidityPeriod(
                            INCOME_TAX_VP1_P1.definitionId,
                        ).definition.nb,
                ),
            ).header(
                "Content-Language",
                SupportedLanguages.NB
                    .toString(),
            )
    }

    @ParameterizedTest
    @MethodSource("internalDefinitionIds")
    fun `get unpublished variable definition`(
        definitionId: String,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("$publicVariableDefinitionsPath/$definitionId")
            .then()
            .statusCode(404)
    }

    @Test
    fun `get request malformed id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("$publicVariableDefinitionsPath/MALFORMED_ID")
            .then()
            .statusCode(400)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("must match"))
    }

    @Test
    fun `get request unknown id`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("$publicVariableDefinitionsPath/${VariableDefinitionService.generateId()}")
            .then()
            .statusCode(404)
            .body(ERROR_MESSAGE_JSON_PATH, containsString("No such variable definition found"))
    }

    @ParameterizedTest
    @CsvSource(
        "1800-01-01, 404, null, null",
        "null, 200, 2021-01-01, null",
        "2021-01-01, 200, 2021-01-01, null",
        "2020-01-01, 200, 1980-01-01, 2020-12-31",
        "2024-06-05, 200, 2021-01-01, null",
        "3000-12-31, 200, 2021-01-01, null",
        nullValues = ["null"],
    )
    fun `get request specific date`(
        dateOfValidity: String?,
        expectedStatusCode: Int,
        expectedValidFrom: String?,
        expectedValidUntil: String?,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .queryParam("date_of_validity", dateOfValidity)
            .`when`()
            .get("$publicVariableDefinitionsPath/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(expectedStatusCode)
            .body("valid_from", equalTo(expectedValidFrom))
            .body("valid_until", equalTo(expectedValidUntil))
    }

    @Test
    fun `get variable definition no value in selected language`(spec: RequestSpecification) {
        val language = SupportedLanguages.EN.toString()
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, language)
            .get("$publicVariableDefinitionsPath/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .assertThat()
            .statusCode(200)
            .body("", hasKey("comment"))
            .body("comment", equalTo(null))
            .header(
                "Content-Language",
                language,
            )
    }

    @Test
    fun `list validity periods`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("$publicVariableDefinitionsPath/${INCOME_TAX_VP1_P1.definitionId}/validity-periods")
            .then()
            .statusCode(200)
            .body("size()", `is`(2))
            .body("[0].valid_from", Matchers.equalTo("1980-01-01"))
            .body("[0].patch_id", Matchers.equalTo(7))
            .body("[1].valid_from", Matchers.equalTo("2021-01-01"))
            .body("[1].patch_id", Matchers.equalTo(6))
            .header(
                "Content-Language",
                SupportedLanguages.NB.toString(),
            )
    }

    @ParameterizedTest
    @MethodSource("internalDefinitionIds")
    fun `list validity periods unpublished variable definition`(
        definitionId: String,
        spec: RequestSpecification,
    ) {
        spec
            .`when`()
            .get("$publicVariableDefinitionsPath/$definitionId/validity-periods")
            .then()
            .statusCode(404)
    }

    @Test
    fun `get request klass codes`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.ACCEPT_LANGUAGE, "nb")
            .queryParam("date_of_validity", "2024-01-01")
            .`when`()
            .get(publicVariableDefinitionsPath)
            .then()
            .assertThat()
            .statusCode(200)
            .body(
                "[0].measurement_type.reference_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/303",
                ),
            ).body("[0].measurement_type.code", equalTo("02.01"))
            .body("[0].measurement_type.title", equalTo("antall"))
            .body(
                "[0].unit_types[0].reference_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/702",
                ),
            ).body("[0].unit_types[0].code", equalTo("01"))
            .body("[0].unit_types[0].title", equalTo("Adresse"))
            .body(
                "[0].subject_fields[0].reference_uri",
                equalTo(
                    "https://www.ssb.no/klass/klassifikasjoner/618",
                ),
            ).body("[0].subject_fields[0].code", equalTo("he04"))
            .body("[0].subject_fields[0].title", equalTo("Helsetjenester"))
    }

    companion object {
        @JvmStatic
        fun internalDefinitionIds(): Stream<Arguments> =
            Stream.of(
                argumentSet("PUBLISHED_INTERNAL", SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId),
                argumentSet("DEPRECATED", SAVED_DEPRECATED_VARIABLE_DEFINITION.definitionId),
                argumentSet("DRAFT", DRAFT_BUS_EXAMPLE.definitionId),
            )
    }
}

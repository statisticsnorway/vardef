package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.DRAFT_BUS_EXAMPLE
import no.ssb.metadata.vardef.utils.ERROR_MESSAGE_JSON_PATH
import no.ssb.metadata.vardef.utils.INCOME_TAX_VP1_P1
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource

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
            .header("Accept-Language", "se")
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
            )
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
            .header("Accept-Language", language.toString())
            .get(publicVariableDefinitionsPath)
            .then()
            .statusCode(200)
            .body("[0].id", notNullValue())
            .body("find { it.short_name == 'bus' }.name", equalTo(DRAFT_BUS_EXAMPLE.name.getValidLanguage(language)))
            .header("Content-Language", language.toString())
    }

    @Test
    fun `get request default language`(spec: RequestSpecification) {
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
            .get("$publicVariableDefinitionsPath/${NanoId.generate(8)}")
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
        spec
            .`when`()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "en")
            .get("$publicVariableDefinitionsPath/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .assertThat()
            .statusCode(200)
            .body("", hasKey("comment"))
            .body("comment", equalTo(null))
    }
}

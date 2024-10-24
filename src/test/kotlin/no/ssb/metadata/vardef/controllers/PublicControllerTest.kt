package no.ssb.metadata.vardef.controllers

import io.micronaut.http.HttpStatus
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.SupportedLanguages
import no.ssb.metadata.vardef.utils.BaseVardefTest
import no.ssb.metadata.vardef.utils.DRAFT_BUS_EXAMPLE
import no.ssb.metadata.vardef.utils.ERROR_MESSAGE_JSON_PATH
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class PublicControllerTest : BaseVardefTest() {
    init {
        RestAssured.filters(RequestLoggingFilter(), ResponseLoggingFilter())
        // Disable auth since we don't expect authentication on public endpoints
        RestAssured.authentication = RestAssured.DEFAULT_AUTH
    }

    private val listVariableDefinitionsPath = "/public/variable-definitions"

    @Test
    fun `list variable definitions incorrect language code`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "se")
            .get(listVariableDefinitionsPath)
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
            .get(listVariableDefinitionsPath)
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
            .get(listVariableDefinitionsPath)
            .then()
            .statusCode(200)
            .body("[0].id", notNullValue())
            .body("find { it.short_name == 'bus' }.name", equalTo(DRAFT_BUS_EXAMPLE.name.getValidLanguage(language)))
            .header("Content-Language", language.toString())
    }
}

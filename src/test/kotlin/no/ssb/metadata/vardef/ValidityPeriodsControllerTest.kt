package no.ssb.metadata.vardef

import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.Matchers.containsString
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDate

class ValidityPeriodsControllerTest : BaseVardefTest() {
    @BeforeEach
    fun setUpValidityPeriods() {
        // Collection of one variable definition
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1960, 1, 1)
                validUntil = LocalDate.of(1980, 11, 30)
            },
        )

        // Start new validity period
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på lørdag",
                        nn = "For personer født på lørdag",
                        en = "Persons born on the Saturday",
                    )
                patchId = 2
            },
        )

        // End validity period
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 3
            },
        )

        // Start new validity period
        variableDefinitionService.save(
            SAVED_VARIABLE_DEFINITION.copy().apply {
                validFrom = LocalDate.of(2021, 1, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på siden",
                        nn = "For personer født på siden",
                        en = "Persons born on the side",
                    )
                patchId = 4
            },
        )

        // Collection of one variable definition
        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION.toSavedVariableDefinition(null))
    }

    /**
     * Input data for testing different scenarios for new validity period.
     */
    companion object {
        @JvmStatic
        fun definitionNotChanged(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2040-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født på siden")
                        put("nn", "For personer født på siden")
                        put("en", "Persons born on the side")
                    }
                }.toString()
            return testCase
        }

        @JvmStatic
        fun newValidityPeriod(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2040-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født i går")
                        put("nn", "For personer født i går")
                        put("en", "person born yesterday")
                    }
                }.toString()
            return testCase
        }

        @JvmStatic
        fun invalidValidFrom(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "1996-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født på siden")
                        put("nn", "For personer født på siden")
                        put("en", "Persons born on the side")
                    }
                }.toString()
            return testCase
        }

        @JvmStatic
        fun invalidValidFromAndInvalidDefinition(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "1996-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født på siden")
                        put("nn", "For personer født på siden")
                        put("en", "Persons born on the side")
                    }
                }.toString()
            return testCase
        }

        @JvmStatic
        fun validFromIsNull(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "null")
                }.toString()
            return testCase
        }
    }

    @Test
    @DisplayName("Post new validity period with new valid from and all defintion texts changed returns 201")
    fun `create new validity period`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriod())
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(201)
    }

    @Test
    @DisplayName("Post new validity period will return 400 if not all languages present are changed")
    fun `create new validity period missing language`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
    }

    @Test
    @DisplayName("Post new validity period will return 400 if not all defintion texts are changed")
    fun `create new validity period definition text is not changed`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChanged())
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    @DisplayName("Post new validity period with valid from between two excisting valid from will return 400")
    fun `create new validity period invalid valid from`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFrom())
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                containsString(
                    "The date selected cannot be added because it falls between previously added valid " +
                        "from dates.",
                ),
            )
    }

    @Test
    @DisplayName("Post new validity period with both invalid valid from and values has an exception hierarchy")
    fun `create new validity period invalid input`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFromAndInvalidDefinition())
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("The date selected cannot be added because it falls between previously added valid from dates."))

        val correctValidFrom = JSONObject(JSON_TEST_INPUT).apply { put("valid_from", "2030-01-11") }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(correctValidFrom)
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    @DisplayName("Post with no valid from will return 400")
    fun `post no valid from`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(validFromIsNull())
            .`when`()
            .post("/variable-definitions/${SAVED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Invalid date format, a valid date follows this format: YYYY-MM-DD",
                ),
            )
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
    @DisplayName("Post new validity period must have valid status")
    fun `create new validity period with invalid status`(
        variableStatus: VariableStatus,
        spec: RequestSpecification,
    ) {
        val id =
            variableDefinitionService
                .save(
                    INPUT_VARIABLE_DEFINITION
                        .apply {
                            this.variableStatus = variableStatus
                        }.toSavedVariableDefinition(1),
                ).definitionId
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/$id/validity-periods")
            .then()
            .statusCode(405)
            .body(containsString("Only allowed for published variables."))
    }
}

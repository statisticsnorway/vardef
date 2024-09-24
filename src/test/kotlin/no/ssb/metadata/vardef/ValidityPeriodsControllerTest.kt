package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.INPUT_VARIABLE_DEFINITION
import no.ssb.metadata.vardef.utils.JSON_TEST_INPUT
import no.ssb.metadata.vardef.utils.VALIDITY_PERIOD
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidityPeriodsControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @BeforeEach
    fun setUpValidityPeriods() {
        variableDefinitionService.clear()

        variableDefinitionService.save(VALIDITY_PERIOD)
        variableDefinitionService.save(
            VALIDITY_PERIOD.copy().apply {
                validUntil = LocalDate.of(2022, 12, 31)
                patchId = 2
            },
        )
        variableDefinitionService.save(
            VALIDITY_PERIOD.copy().apply {
                validFrom = LocalDate.of(2023, 1, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på siden",
                        nn = "For personer født på siden",
                        en = "Persons born on the side",
                    )
                patchId = 3
            },
        )
    }

    /**
     * test cases new validity period.
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
                    remove("short_name")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun definitionNotChangedForAll(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2040-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født på mandag")
                        put("nn", "For personer født på mandag")
                        put("en", "Persons born on the side")
                    }
                    remove("short_name")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun newValidityPeriod(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2024-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født i går")
                        put("nn", "For personer født i går")
                        put("en", "Persons born yesterday")
                    }
                    remove("short_name")
                    remove("valid_until")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun newValidityPeriodBeforeAll(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "1923-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født hver dag")
                        put("nn", "For personer født hver dag")
                        put("en", "Persons born every day")
                    }
                    remove("short_name")
                    remove("valid_until")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun invalidValidFrom(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2021-05-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født på søndag")
                        put("nn", "For personer født på søndag")
                        put("en", "Persons born on sunday")
                    }
                    remove("short_name")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun invalidValidFromAndInvalidDefinition(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2021-05-11")
                    remove("short_name")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun validFromIsNull(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "null")
                    remove("short_name")
                }.toString()
            return testCase
        }

        @JvmStatic
        fun shortNameIsIncluded(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "2024-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født i går")
                        put("nn", "For personer født i går")
                        put("en", "Persons born yesterday")
                    }
                }.toString()
            return testCase
        }
    }

    @Test
    fun `create new validity period`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriod())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(201)
        val lastPatchId = variableDefinitionService.getLatestPatchById(VALIDITY_PERIOD.definitionId).patchId
        assertThat(
            variableDefinitionService.getLatestPatchById(
                VALIDITY_PERIOD.definitionId,
            ).patchId,
        ).isEqualTo(lastPatchId)
        assertThat(
            variableDefinitionService.getLatestPatchById(
                VALIDITY_PERIOD.definitionId,
            ).validUntil,
        ).isNull()
        assertThat(
            variableDefinitionService.getOnePatchById(
                VALIDITY_PERIOD.definitionId,
                lastPatchId - 1,
            ).validUntil,
        ).isNotNull()
    }

    @Test
    fun `create new validity period before all validity periods`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriodBeforeAll())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(201)
        val lastPatchId = variableDefinitionService.getLatestPatchById(VALIDITY_PERIOD.definitionId).patchId
        assertThat(
            variableDefinitionService.getLatestPatchById(
                VALIDITY_PERIOD.definitionId,
            ).patchId,
        ).isEqualTo(lastPatchId)
        assertThat(
            variableDefinitionService.getLatestPatchById(
                VALIDITY_PERIOD.definitionId,
            ).validUntil,
        ).isEqualTo(LocalDate.of(2020, 12, 31))
    }

    @Test
    fun `create new validity period missing language`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChangedForAll())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(400)
    }

    @Test
    fun `create new validity period definition text is not changed`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChanged())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period invalid valid from`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFrom())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
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
    fun `create new validity period invalid input`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFromAndInvalidDefinition())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("The date selected cannot be added because it falls between previously added valid from dates."))

        val correctValidFrom = JSONObject(invalidValidFromAndInvalidDefinition()).apply { put("valid_from", "2030-01-11") }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(correctValidFrom)
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period no valid from`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(validFromIsNull())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "Failed to convert argument [newPeriod] for value [null]",
                ),
            )
    }

    @Test
    fun `create new validity period with new short name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(shortNameIsIncluded())
            .`when`()
            .post("/variable-definitions/${VALIDITY_PERIOD.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "short_name may not be specified here",
                ),
            )
    }

    @ParameterizedTest
    @EnumSource(value = VariableStatus::class, names = ["PUBLISHED.*"], mode = EnumSource.Mode.MATCH_NONE)
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
                        }.toSavedVariableDefinition(),
                ).definitionId
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriod())
            .`when`()
            .post("/variable-definitions/$id/validity-periods")
            .then()
            .statusCode(405)
            .body(containsString("Only allowed for published variables."))
    }
}

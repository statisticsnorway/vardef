package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.models.VariableStatus
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
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

    private val savedVariableDefinition = SAVED_VARIABLE_DEFINITION.copy()

    @BeforeEach
    fun setUp() {
        variableDefinitionService.clear()

        // Collection of one variable definition
        variableDefinitionService.save(savedVariableDefinition)
        variableDefinitionService.save(savedVariableDefinition.copy().apply { patchId = 2 })
        variableDefinitionService.save(savedVariableDefinition.copy().apply { patchId = 3 })
        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 4
            },
        )
        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 5
            },
        )
        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(1980, 12, 1)
                validUntil = LocalDate.of(2020, 12, 31)
                patchId = 6
            },
        )

        variableDefinitionService.save(
            savedVariableDefinition.copy().apply {
                validFrom = LocalDate.of(2021, 1, 1)
                validUntil = null
                definition =
                    LanguageStringType(
                        nb = "For personer født på siden",
                        nn = "For personer født på siden",
                        en = "Persons born on the side",
                    )
                patchId = 7
            },
        )

        variableDefinitionService.save(INPUT_VARIABLE_DEFINITION.toSavedVariableDefinition(null))
    }

    companion object {
        @JvmStatic
        fun postValidityPeriodDefinitionNotChanged(): String {
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
        fun postValidityPeriodOk(): String {
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
        fun postValidityPeriodInvalidValidFrom(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "1996-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født i går")
                        put("nn", "For personer født i går")
                        put("en", "person born yesterday")
                    }
                }.toString()
            return testCase
        }

        @JvmStatic
        fun postValidityPeriodInvalidValidFromAndInvalidDefinition(): String {
            val testCase =
                JSONObject(JSON_TEST_INPUT).apply {
                    put("valid_from", "1996-01-11")
                    getJSONObject("definition").apply {
                        put("nb", "For personer født")
                        put("nn", "For personer født")
                        put("en", "person born yesterday")
                    }
                }.toString()
            return testCase
        }
    }

    @Test
    fun `create new validity period not all definitions changed`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(JSON_TEST_INPUT)
            .`when`()
            .post("/variable-definitions/${savedVariableDefinition.definitionId}/validity-periods")
            .then()
            .statusCode(400)
    }

    @Test
    fun `create new validity period all definitions in all languages are changed`(spec: RequestSpecification) {
        val modifiedJson: String = postValidityPeriodOk()
        val previousPatchId = variableDefinitionService.getLatestPatchById(savedVariableDefinition.definitionId).patchId

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${savedVariableDefinition.definitionId}/validity-periods")
            .then()
            .statusCode(201)
            .body("patch_id", equalTo(previousPatchId + 2))
    }

    @Test
    fun `create new validity period definition text is not changed`(spec: RequestSpecification) {
        val modifiedJson: String = postValidityPeriodDefinitionNotChanged()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${savedVariableDefinition.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period invalid valid from`(spec: RequestSpecification) {
        val modifiedJson: String = postValidityPeriodInvalidValidFrom()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${savedVariableDefinition.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("The date selected cannot be added because it falls between previously added valid from dates."))
    }

    @Test
    fun `create new validity period invalid valid from and not changed definition`(spec: RequestSpecification) {
        val modifiedJson: String = postValidityPeriodInvalidValidFromAndInvalidDefinition()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(modifiedJson)
            .`when`()
            .post("/variable-definitions/${savedVariableDefinition.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("The date selected cannot be added because it falls between previously added valid from dates."))

        val correctValidFrom = JSONObject(JSON_TEST_INPUT).apply { put("valid_from", "2030-01-11") }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(correctValidFrom)
            .`when`()
            .post("/variable-definitions/${savedVariableDefinition.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
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
                        }.toSavedVariableDefinition(null),
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

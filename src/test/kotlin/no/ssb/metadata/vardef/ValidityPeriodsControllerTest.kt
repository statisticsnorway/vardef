package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.viascom.nanoid.NanoId
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.bson.types.ObjectId
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidityPeriodsControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    val savedVariableDefinition =
        SavedVariableDefinition(
            id = ObjectId(),
            definitionId = NanoId.generate(8),
            patchId = 1,
            name =
                LanguageStringType(
                    nb = "Landbakgrunn",
                    nn = "Landbakgrunn",
                    en = "Country Background",
                ),
            shortName = "landbak",
            definition =
                LanguageStringType(
                    nb = "For personer født",
                    nn = "For personer født",
                    en = "Country background is",
                ),
            classificationUri = "91",
            unitTypes = listOf("01", "02"),
            subjectFields = listOf("he04"),
            containsSensitivePersonalInformation = false,
            variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
            measurementType = "02.01",
            validFrom = LocalDate.of(1960, 1, 1),
            validUntil = LocalDate.of(1980, 11, 30),
            externalReferenceUri = URI("https://example.com/").toURL(),
            relatedVariableDefinitionUris = listOf(),
            owner =
                Owner("", ""),
            contact =
                Contact(
                    LanguageStringType("", "", ""),
                    "me@example.com",
                ),
            createdAt = LocalDateTime.parse("2024-06-11T08:15:19"),
            createdBy =
                Person("", ""),
            lastUpdatedAt = LocalDateTime.parse("2024-06-11T08:15:19"),
            lastUpdatedBy =
                Person("", ""),
        )

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
        // if latest validity period == null, two patches up
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
            .body(containsString("Not valid date"))
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
            .body(containsString("Not valid date"))

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

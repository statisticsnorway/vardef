package no.ssb.metadata.vardef

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import jakarta.inject.Inject
import no.ssb.metadata.vardef.models.LanguageStringType
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.json.JSONObject
import org.junit.jupiter.api.*
import java.time.LocalDate

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ValidityPeriodsControllerTest {
    @Inject
    lateinit var variableDefinitionService: VariableDefinitionService

    @BeforeEach
    fun setUpValidityPeriods() {
        variableDefinitionService.clear()

        // collection 1
        variableDefinitionService.save(SAVED_FNR_EXAMPLE)
        variableDefinitionService.save(
            SAVED_FNR_EXAMPLE.copy().apply {
                validUntil = LocalDate.of(2022, 12, 31)
                patchId = 2
            },
        )
        variableDefinitionService.save(
            SAVED_FNR_EXAMPLE.copy().apply {
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

        // collection 2
        variableDefinitionService.save(SAVED_DRAFT_DEADWEIGHT_EXAMPLE)

        // collection3
        variableDefinitionService.save(SAVED_DEPRECATED_VARIABLE_DEFINITION)
    }

    /**
     * test cases new validity period.
     */
    companion object {

        @JvmStatic
        fun newValidityPeriod(): String {
            val testCase =
                JSONObject().apply {
                    put("valid_from", "2024-01-11")
                    put("definition", JSONObject().apply {
                        put("nb", "For personer født i går")
                        put("nn", "For personer født i går")
                        put("en", "Persons born yesterday")
                    })
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
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(201)

        val lastPatch = variableDefinitionService.getLatestPatchById(SAVED_FNR_EXAMPLE.definitionId)

        assertThat(
            variableDefinitionService.getLatestPatchById(
                SAVED_FNR_EXAMPLE.definitionId,
            ).validUntil,
        ).isNull()
        assertThat(
            variableDefinitionService.getOnePatchById(
                SAVED_FNR_EXAMPLE.definitionId,
                lastPatch.patchId - 1,
            ).validUntil,
        ).isEqualTo(lastPatch.validFrom.minusDays(1))
    }

    @Test
    fun `create new validity period before all validity periods`(spec: RequestSpecification) {

        val newValidityPeriodBeforeAll =
            JSONObject().apply {
                put("valid_from", "1923-01-11")
                put("definition", JSONObject().apply {
                    put("nb", "For personer født hver dag")
                    put("nn", "For personer født hver dag")
                    put("en", "Persons born every day")
                })
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriodBeforeAll)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(201)
        assertThat(
            variableDefinitionService.getLatestPatchById(
                SAVED_FNR_EXAMPLE.definitionId,
            ).validUntil,
        ).isEqualTo(LocalDate.of(2020, 12, 31))
    }

    @Test
    fun `create new validity period missing language`(spec: RequestSpecification) {

        val definitionNotChangedForAll =
            JSONObject().apply {
                put("valid_from", "2040-01-11")
                put("definition", JSONObject().apply {
                    put("nb", "For personer født på mandag")
                    put("nn", "For personer født på mandag")
                    put("en", "Persons born on the side")
                })
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChangedForAll)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(400)
    }

    @Test
    fun `create new validity period definition text is not changed`(spec: RequestSpecification) {

        val definitionNotChanged =
            JSONObject().apply {
                put("valid_from", "2040-01-11")
                put("definition", JSONObject().apply {
                    put("nb", "For personer født på siden")
                    put("nn", "For personer født på siden")
                    put("en", "Persons born on the side")
                })
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(definitionNotChanged)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period invalid valid from`(spec: RequestSpecification) {

        val invalidValidFrom =
            JSONObject().apply {
                put("valid_from", "2021-05-11")
                put("definition", JSONObject().apply {
                    put("nb", "For personer født på søndag")
                    put("nn", "For personer født på søndag")
                    put("en", "Persons born on sunday")
                })
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFrom)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
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

        val invalidValidFromAndInvalidDefinition =
            JSONObject().apply {
                put("valid_from", "2021-05-11")
                put("definition", JSONObject().apply {
                    put("nb", "For personer født på siden")
                    put("nn", "For personer født på siden")
                    put("en", "Persons born on the side")
                })
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(invalidValidFromAndInvalidDefinition)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("The date selected cannot be added because it falls between previously added valid from dates."))

        val correctValidFrom = JSONObject(invalidValidFromAndInvalidDefinition).apply { put("valid_from", "2030-01-11") }.toString()
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(correctValidFrom)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(containsString("Definition text for all languages must be changed when creating a new validity period."))
    }

    @Test
    fun `create new validity period no valid from`(spec: RequestSpecification) {

        val validFromIsNull =
            JSONObject(newValidityPeriod()).apply {
                remove("valid_from")
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(validFromIsNull)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
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

        val newShortName =
            JSONObject().apply {
                put("valid_from", "2024-01-11")
                put("definition", JSONObject().apply {
                    put("nb", "For personer født i går")
                    put("nn", "For personer født i går")
                    put("en", "Persons born yesterday")
                })
                put("short_name","car")
            }.toString()

        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newShortName)
            .`when`()
            .post("/variable-definitions/${SAVED_FNR_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(400)
            .body(
                "_embedded.errors[0].message",
                containsString(
                    "short_name may not be specified here",
                ),
            )
    }

    @Test
    fun `create new validity period with draft status`(spec: RequestSpecification) {


        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriod())
            .`when`()
            .post("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}/validity-periods")
            .then()
            .statusCode(405)
    }

    @Test
    fun `create new validity period with deprecated status`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(newValidityPeriod())
            .`when`()
            .post("/variable-definitions/${SAVED_DEPRECATED_VARIABLE_DEFINITION.definitionId}/validity-periods")
            .then()
            .statusCode(405)
    }
}

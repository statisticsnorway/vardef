package no.ssb.metadata.vardef.controllers.variabledefinitionbyid

import io.micronaut.http.HttpStatus
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import no.ssb.metadata.vardef.models.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import no.ssb.metadata.vardef.utils.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.*
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class UpdateTests : BaseVardefTest() {
    @Test
    fun `update variable definition`(spec: RequestSpecification) {
        val expected: SavedVariableDefinition =
            SAVED_DRAFT_DEADWEIGHT_EXAMPLE.copy(
                name = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.name.copy(en = "Update"),
            )

        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"name": {
                        "nb": "Dødvekt",
                        "nn": "Dødvekt",
                        "en": "Update"
                    }}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${expected.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()
        val body = jsonMapper.readValue(bodyString, CompleteView::class.java)

        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
        assertThat(body.name).isEqualTo(expected.name)
        assertThat(body.definition).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definition)

        val updated: SavedVariableDefinition = patches.latest(expected.definitionId)
        assertThat(updated.definitionId).isEqualTo(expected.definitionId)
        assertThat(updated.createdAt).isCloseTo(expected.createdAt, within(1, ChronoUnit.SECONDS))
        assertThat(updated.name).isEqualTo(expected.name)
        assertThat(updated.lastUpdatedAt).isAfter(expected.lastUpdatedAt)
    }

    @Test
    fun `update variable with published status`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(405)
    }

    @Test
    fun `update draft variable definition with new short name`(spec: RequestSpecification) {
        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name":"hoppebek"}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()

        val body = jsonMapper.readValue(bodyString, CompleteView::class.java)
        assertThat(body.shortName).isNotEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.shortName)
        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
    }

    @Test
    fun `update draft variable definition with another short name that is already in use`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("""{"short_name": "${INCOME_TAX_VP1_P1.shortName}"}""")
            .`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "is already in use by another variable definition.",
                ),
            )
    }

    @Test
    fun `update draft variable definition with unchanged short name`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body("""{"short_name": "${DRAFT_BUS_EXAMPLE.shortName}"}""")
            .`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.OK.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.utils.TestUtils#invalidVariableDefinitions")
    fun `update draft variable definition with invalid inputs`(
        updatedJsonString: String,
        constraintViolation: Boolean,
        fieldName: String?,
        errorMessage: String?,
        spec: RequestSpecification,
    ) {
        spec
            .contentType(ContentType.JSON)
            .body(updatedJsonString)
            .`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(buildProblemJsonResponseSpec(constraintViolation, fieldName, errorMessage))
    }

    @Test
    fun `update draft variable definition with malformed id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/MALFORMED_ID")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.code)
    }

    @Test
    fun `update draft variable definition with unknown id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${VariableDefinitionService.generateId()}")
            .then()
            .statusCode(404)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "No such variable definition found",
                ),
            )
    }

    @Test
    fun `update draft variable definition attempt to modify id`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"id":  "${VariableDefinitionService.generateId()}",
                "name": {
                    "nb": "Landbakgrunn",
                    "nn": "Landbakgrunn",
                    "en": "Update"
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(400)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Unknown property [id] encountered during deserialization of type",
                ),
            )
        assertThat(
            variableDefinitionService
                .list()
                .map { it.definitionId },
        ).contains(INCOME_TAX_VP1_P1.definitionId)
        assertThat(variableDefinitionService.list().map { it.name }).contains(INCOME_TAX_VP1_P1.name)
    }

    @Test
    fun `variable definition has comment field`(spec: RequestSpecification) {
        spec
            .`when`()
            .get("/variable-definitions/${INCOME_TAX_VP1_P1.definitionId}")
            .then()
            .statusCode(200)
            .body("comment", not(equalTo(null)))
    }

    @Test
    fun `update draft variable definition add new languages to comment`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"comment": {
                    "nb": "Legger til merknad",
                    "nn": "Endrer merknad",
                    "en": null
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Legger til merknad"))
            .body("comment.nn", containsString("Endrer merknad"))
            .body("comment.en", containsString("Adding comment"))
    }

    @Test
    fun `update draft variable definition add comment existing comment is null`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"comment": {
                    "nb": "Legger til merknad",
                    "nn": "Endrer merknad",
                    "en": null
                }}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${DRAFT_BUS_EXAMPLE.definitionId}")
            .then()
            .statusCode(200)
            .body("comment.nb", containsString("Legger til merknad"))
            .body("comment.nn", containsString("Endrer merknad"))
            .body("comment.en", equalTo(null))
    }

    @Test
    fun `update draft variable definition return complete view`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name": "nothing"}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .body("", hasKey("owner"))
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView).isNotNull
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitionbyid.CompanionObject#validOwnerUpdates")
    fun `update owner`(
        valueBeforeUpdate: String,
        jsonInput: String,
        pathVariable: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonInput,
            ).`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body(pathVariable, not(equalTo(valueBeforeUpdate)))
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitionbyid.CompanionObject#invalidOwnerUpdates")
    fun `update owner bad request`(
        jsonInput: String,
        constraintViolation: Boolean,
        expectedErrorMessage: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonInput,
            ).`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    constraintViolation = constraintViolation,
                    fieldName = null,
                    errorMessage = expectedErrorMessage,
                ),
            )

        val savedVariableDefinition =
            variableDefinitionService.getCompleteByDateAndStatus(
                SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId,
            )
        assertThat(savedVariableDefinition?.owner?.team).isNotBlank()
        assertThat(savedVariableDefinition?.owner?.groups?.all { it.isNotBlank() })
        assertThat(savedVariableDefinition?.owner?.groups?.isNotEmpty())
    }

    @Test
    fun `update variable definition not all languages`(spec: RequestSpecification) {
        val expected: SavedVariableDefinition =
            SAVED_DRAFT_DEADWEIGHT_EXAMPLE.copy(
                name = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.name.copy(en = "Update", nn = "Dødvekt", nb = "Dødvekt"),
                definition =
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definition.copy(
                        en = "Update",
                        nn = "Dødvekt er den største vekta skipet kan bera av last og behaldningar.",
                        nb = "Dødvekt er den største vekt skipet kan bære av last og beholdninger.",
                    ),
                comment =
                    SAVED_DRAFT_DEADWEIGHT_EXAMPLE.comment?.copy(
                        nb = "Update",
                        en = "Adding comment",
                        nn = "Updated comment",
                    ),
            )
        val bodyString =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"name": {
                        "en": "Update"
                    },
                    "definition": {
                        "en": "Update"
                    },
                    "comment": {
                        "nb": "Update",
                        "nn": "Updated comment"
                    }}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${expected.definitionId}")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract()
                .body()
                .asString()
        val body = jsonMapper.readValue(bodyString, CompleteView::class.java)

        assertThat(body.id).isEqualTo(SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId)
        assertThat(body.name).isEqualTo(expected.name)
        assertThat(body.definition).isEqualTo(expected.definition)
        assertThat(body.comment).isEqualTo(expected.comment)
    }

    @Test
    fun `created by is not changed when updated by another user`(spec: RequestSpecification) {
        val createdBy = SAVED_DRAFT_DEADWEIGHT_EXAMPLE.createdBy
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """
                    {"short_name": "nothing"}
                    """.trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.createdBy).isEqualTo(createdBy)
        assertThat(completeView.lastUpdatedBy).isNotEqualTo(createdBy)
        assertThat(completeView.lastUpdatedBy).isEqualTo(TEST_USER)
    }

    @Test
    fun `publish variable with illegal shortname`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"variable_status": "PUBLISHED_INTERNAL"}
                """.trimIndent(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME.definitionId}")
            .then()
            .log()
            .everything()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    "The short name ${SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME.shortName} " +
                        "is illegal and must be changed before it is published",
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitionbyid.CompanionObject#inValidDateUpdates")
    fun `invalid date update`(
        input: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                input,
            ).`when`()
            .patch("/variable-definitions/${DRAFT_EXAMPLE_WITH_VALID_UNTIL.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    false,
                    null,
                    errorMessage = "Invalid date order",
                ),
            )
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitionbyid.CompanionObject#validDateUpdates")
    fun `valid date update`(
        input: String,
        field: String,
        valueBeforeUpdate: String,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .patch("/variable-definitions/${DRAFT_EXAMPLE_WITH_VALID_UNTIL.definitionId}")
            .then()
            .statusCode(HttpStatus.OK.code)
            .body(field, not(valueBeforeUpdate))
    }

    @Test
    fun `publish variable with valid until`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    """{"variable_status": "PUBLISHED_INTERNAL"}""".trimIndent(),
                ).`when`()
                .patch("/variable-definitions/${DRAFT_EXAMPLE_WITH_VALID_UNTIL.definitionId}")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.variableStatus).isEqualTo(VariableStatus.PUBLISHED_INTERNAL)
        assertThat(completeView.validUntil).isEqualTo(LocalDate.of(2030, 9, 15))
    }

    @Test
    fun `publish variable externally with missing languages`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonMapper.writeValueAsString(UpdateDraft(variableStatus = VariableStatus.PUBLISHED_EXTERNAL)),
            ).`when`()
            .patch("/variable-definitions/${DRAFT_EXAMPLE_WITH_VALID_UNTIL.definitionId}")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
    }

    @Test
    fun `publish variable externally with all languages`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonMapper.writeValueAsString(
                    UpdateDraft(
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                ),
            ).`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.OK.code)
    }

    @Test
    fun `publish variable externally while filling out all languages`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonMapper.writeValueAsString(
                    UpdateDraft(
                        definition = LanguageStringType(nb = "something", nn = "something", "something"),
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                ),
            ).`when`()
            .patch("/variable-definitions/${DRAFT_EXAMPLE_WITH_VALID_UNTIL.definitionId}")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
    }

    @Test
    fun `publish variable externally while removing a language`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                jsonMapper.writeValueAsString(
                    UpdateDraft(
                        definition = LanguageStringType(nb = null, nn = "something", "something"),
                        variableStatus = VariableStatus.PUBLISHED_EXTERNAL,
                    ),
                ),
            ).`when`()
            .patch("/variable-definitions/${SAVED_DRAFT_DEADWEIGHT_EXAMPLE.definitionId}")
            .then()
            .statusCode(HttpStatus.CONFLICT.code)
    }

    @ParameterizedTest
    @MethodSource("no.ssb.metadata.vardef.controllers.variabledefinitionbyid.CompanionObject#updateMandatoryFields")
    fun `attempt to update variable mandatory fields`(
        input: String,
        errorMessage: String?,
        spec: RequestSpecification,
    ) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(input)
            .`when`()
            .patch("/variable-definitions/${SAVED_TO_PUBLISH.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
            .spec(
                buildProblemJsonResponseSpec(
                    true,
                    null,
                    errorMessage = errorMessage,
                ),
            )
    }

    @Test
    fun `publish variable definition illegal shortname`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                JSONObject()
                    .apply {
                        put("variable_status", "PUBLISHED_INTERNAL")
                    }.toString(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_BYDEL_WITH_ILLEGAL_SHORTNAME.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
    }

    @Test
    fun `publish variable definition contact`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                JSONObject()
                    .apply {
                        put("variable_status", "PUBLISHED_INTERNAL")
                    }.toString(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_TO_PUBLISH.definitionId}")
            .then()
            .statusCode(HttpStatus.OK.code)
    }

    @Test
    fun `attempt to publish generated contact`(spec: RequestSpecification) {
        spec
            .given()
            .contentType(ContentType.JSON)
            .body(
                JSONObject()
                    .apply {
                        put("variable_status", "PUBLISHED_INTERNAL")
                    }.toString(),
            ).`when`()
            .patch("/variable-definitions/${SAVED_TO_PUBLISH_ILLEGAL_CONTACT.definitionId}")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.code)
    }

    @Test
    fun `update containsSpecialCategoriesOfPersonalData`(spec: RequestSpecification) {
        val body =
            spec
                .given()
                .contentType(ContentType.JSON)
                .body(
                    JSONObject()
                        .apply {
                            put("contains_special_categories_of_personal_data", "")
                        }.toString(),
                ).`when`()
                .patch("/variable-definitions/${SAVED_TO_PUBLISH.definitionId}")
                .then()
                .statusCode(HttpStatus.OK.code)
                .extract()
                .body()
                .asString()

        val completeView = jsonMapper.readValue(body, CompleteView::class.java)
        assertThat(completeView.containsSpecialCategoriesOfPersonalData).isEqualTo(false)
    }
}

package no.ssb.metadata.vardef.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import io.micronaut.problem.ProblemJsonErrorResponseBodyProvider.APPLICATION_PROBLEM_JSON
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.specification.ResponseSpecification
import no.ssb.metadata.vardef.models.CompleteView
import no.ssb.metadata.vardef.models.RenderedView
import no.ssb.metadata.vardef.models.VariableStatus
import org.hamcrest.Matchers.*
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

const val PROBLEM_JSON_DETAIL_JSON_PATH = "detail"
const val PROBLEM_JSON_VIOLATIONS_FIELD_JSON_PATH = "violations.field"
const val PROBLEM_JSON_VIOLATIONS_MESSAGE_JSON_PATH = "violations.message"

/**
 * A custom appender for logging events used in testing scenarios.
 *
 * This class extends [AppenderBase] and implements a simple in-memory appender
 * that collects log messages for inspection. It allows you to capture logs generated
 * during tests and later retrieve or clear them for validation purposes.
 *
 * @constructor Creates a [TestLogAppender] instance.
 */
class TestLogAppender : AppenderBase<ILoggingEvent>() {
    private val logMessages = mutableListOf<ILoggingEvent>()

    override fun append(eventObject: ILoggingEvent?) {
        if (eventObject != null) {
            logMessages.add(eventObject)
        }
    }

    fun getLoggedMessages(): List<ILoggingEvent> = logMessages

    fun reset() = logMessages.clear()
}

/**
 * Build a reusable specification for asserting Problem JSON fields
 *
 * @param constraintViolation true if the format follows [Constraint Violation format](https://opensource.zalando.com/problem/constraint-violation/), false for the base Problem JSON format.
 * @param fieldName optional, to assert on whether the field is included in the body
 * @param errorMessage optional, to assert on whether the error message is included in the body
 * @return the built [ResponseSpecification]
 */
fun buildProblemJsonResponseSpec(
    constraintViolation: Boolean,
    fieldName: String?,
    errorMessage: String?,
): ResponseSpecification {
    val builder = ResponseSpecBuilder()
    builder.expectContentType(APPLICATION_PROBLEM_JSON)
    if (constraintViolation) {
        fieldName?.let {
            builder
                .expectBody(
                    PROBLEM_JSON_VIOLATIONS_FIELD_JSON_PATH,
                    hasItems(containsString(it)),
                )
        }
        errorMessage?.let {
            builder
                .expectBody(
                    PROBLEM_JSON_VIOLATIONS_MESSAGE_JSON_PATH,
                    hasItems(containsString(it)),
                )
        }
    } else {
        fieldName?.let {
            builder
                .expectBody(
                    PROBLEM_JSON_DETAIL_JSON_PATH,
                    containsString(it),
                )
        }
        errorMessage?.let {
            builder
                .expectBody(
                    PROBLEM_JSON_DETAIL_JSON_PATH,
                    containsString(it),
                )
        }
    }
    return builder.build()
}

object TestUtils {
    /**
     * Definition ids for variables with all variable statuses and variable status value
     */
    @JvmStatic
    fun definitionIdsAllStatuses(): Stream<Arguments> =
        Stream.of(
            argumentSet("Published external", INCOME_TAX_VP1_P1.definitionId, "PUBLISHED_EXTERNAL"),
            argumentSet("Published internal", SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId, "PUBLISHED_INTERNAL"),
            argumentSet("CreateDraft", DRAFT_BUS_EXAMPLE.definitionId, "DRAFT"),
        )

    /**
     * Formats for controller methods which have a `render` query parameter
     */
    @JvmStatic
    fun returnFormats(): Stream<Arguments> =
        Stream.of(
            argumentSet("Rendered", true, RenderedView::class.java),
            argumentSet("Not rendered", false, CompleteView::class.java),
            argumentSet("Null", null, CompleteView::class.java),
        )

    /**
     * Formats for controller methods which have a `render` query parameter
     */
    @JvmStatic
    fun returnFormatsArrays(): Stream<Arguments> =
        Stream.of(
            argumentSet("Rendered", true, Array<RenderedView>::class.java),
            argumentSet("Not rendered", false, Array<CompleteView>::class.java),
            argumentSet("Null", null, Array<CompleteView>::class.java),
        )

    /**
     * Invalid variable definitions.
     *
     * Some fields are not included in these test cases because they're covered by other tests. They include:
     * - id
     * - variable_status
     *
     * @return
     */
    @JvmStatic
    fun invalidVariableDefinitions(): Stream<Arguments> =
        Stream.of(
            argumentSet(
                "Unknown language",
                jsonTestInput()
                    .apply {
                        getJSONObject("name").apply {
                            remove("en")
                            put(
                                "se",
                                "Landbakgrunn",
                            )
                        }
                    }.toString(),
                false,
                "name",
                "Unknown property [se]",
            ),
            argumentSet(
                "short_name with dashes",
                jsonTestInput().apply { put("short_name", "dash-not-allowed") }.toString(),
                true,
                "shortName",
                "must match",
            ),
            argumentSet(
                "short_name with capital letters",
                jsonTestInput().apply { put("short_name", "CAPITALS") }.toString(),
                true,
                "shortName",
                "must match",
            ),
            argumentSet(
                "short_name too short",
                jsonTestInput().apply { put("short_name", "a") }.toString(),
                true,
                "shortName",
                "must match",
            ),
            argumentSet(
                "classification_reference invalid",
                jsonTestInput().apply { put("classification_reference", "100000") }.toString(),
                true,
                "classificationReference",
                "Code 100000 is not a valid classification id",
            ),
            argumentSet(
                "unit_types invalid code",
                jsonTestInput().apply { put("unit_types", listOf("blah")) }.toString(),
                true,
                "unitTypes",
                "Code blah is not a member of classification with id",
            ),
            argumentSet(
                "subject_fields invalid code",
                jsonTestInput().apply { put("subject_fields", listOf("blah")) }.toString(),
                true,
                "subjectFields",
                "Code blah is not a member of classification with id",
            ),
            argumentSet(
                "measurement_type invalid code",
                jsonTestInput().apply { put("measurement_type", "blah") }.toString(),
                true,
                "measurementType",
                "Code blah is not a member of classification with id",
            ),
            argumentSet(
                "valid_from invalid date",
                jsonTestInput().apply { put("valid_from", "2024-20-11") }.toString(),
                false,
                null,
                "Error decoding property [LocalDate validFrom]",
            ),
            argumentSet(
                "external_reference_uri invalid",
                jsonTestInput().apply { put("external_reference_uri", "Not url") }.toString(),
                false,
                null,
                "Not url",
            ),
            argumentSet(
                "related_variable_definition_uris malformed uri",
                jsonTestInput()
                    .apply {
                        put(
                            "related_variable_definition_uris",
                            listOf("not a url"),
                        )
                    }.toString(),
                false,
                null,
                "no protocol",
            ),
            argumentSet(
                "contact malformed email",
                jsonTestInput()
                    .apply {
                        getJSONObject("contact").put(
                            "email",
                            "not an email",
                        )
                    }.toString(),
                true,
                "contact",
                "must be a well-formed email address",
            ),
        )

    @JvmStatic
    fun variableDefinitionsNonMandatoryFieldsRemoved(): Stream<Arguments> =
        Stream.of("measurement_type", "valid_until", "external_reference_uri", "related_variable_definition_uris").map {
            argumentSet(
                "$it removed",
                jsonTestInput()
                    .apply {
                        remove(it)
                    }.toString(),
            )
        }

    @JvmStatic
    fun draftVariableDefinitionMandatoryFieldsRemoved(): Stream<Arguments> =
        Stream.of("name", "short_name", "definition", "valid_from").map {
            argumentSet(
                "$it removed",
                jsonTestInput()
                    .apply {
                        remove(it)
                    }.toString(),
            )
        }

    @JvmStatic
    fun variableDefinitionsVariousVariableStatus(): Stream<Arguments.ArgumentSet> =
        VariableStatus.entries
            .map { it.name }
            .plus("Not a status")
            .map {
                argumentSet(
                    "'$it' status",
                    jsonTestInput()
                        .apply {
                            put("variable_status", it)
                        }.toString(),
                )
            }.stream()
}

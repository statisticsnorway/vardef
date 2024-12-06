package no.ssb.metadata.vardef.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import no.ssb.metadata.vardef.models.VariableStatus
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

const val ERROR_MESSAGE_JSON_PATH = "_embedded.errors[0].message"

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

object TestUtils {
    /**
     * Definition ids for variables with all variable statuses and variable status value
     */
    @JvmStatic
    fun definitionIdsAllStatuses(): Stream<Arguments> =
        Stream.of(
            argumentSet("Published external", INCOME_TAX_VP1_P1.definitionId, "PUBLISHED_EXTERNAL"),
            argumentSet("Published internal", SAVED_INTERNAL_VARIABLE_DEFINITION.definitionId, "PUBLISHED_INTERNAL"),
            argumentSet("Draft", DRAFT_BUS_EXAMPLE.definitionId, "DRAFT"),
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
                "Unknown property [se]",
            ),
            argumentSet(
                "short_name with dashes",
                jsonTestInput().apply { put("short_name", "dash-not-allowed") }.toString(),
                "shortName: must match",
            ),
            argumentSet(
                "short_name with capital letters",
                jsonTestInput().apply { put("short_name", "CAPITALS") }.toString(),
                "shortName: must match",
            ),
            argumentSet(
                "short_name too short",
                jsonTestInput().apply { put("short_name", "a") }.toString(),
                "shortName: must match",
            ),
            argumentSet(
                "classification_reference invalid",
                jsonTestInput().apply { put("classification_reference", "100000") }.toString(),
                "classificationReference: Code 100000 is not a valid classification id",
            ),
            argumentSet(
                "unit_types invalid code",
                jsonTestInput().apply { put("unit_types", listOf("blah")) }.toString(),
                "Code blah is not a member of classification with id",
            ),
            argumentSet(
                "subject_fields invalid code",
                jsonTestInput().apply { put("subject_fields", listOf("blah")) }.toString(),
                "Code blah is not a member of classification with id",
            ),
            argumentSet(
                "measurement_type invalid code",
                jsonTestInput().apply { put("measurement_type", "blah") }.toString(),
                "Code blah is not a member of classification with id",
            ),
            argumentSet(
                "valid_from invalid date",
                jsonTestInput().apply { put("valid_from", "2024-20-11") }.toString(),
                "Error deserializing type",
            ),
            argumentSet(
                "valid_until specified",
                jsonTestInput().apply { put("valid_until", "2030-06-30") }.toString(),
                "valid_until may not be specified here",
            ),
            argumentSet(
                "external_reference_uri invalid",
                jsonTestInput().apply { put("external_reference_uri", "Not url") }.toString(),
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
    fun variableDefinitionsMandatoryFieldsRemoved(): Stream<Arguments> =
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

package no.ssb.metadata.vardef.utils

import no.ssb.metadata.vardef.models.VariableStatus
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

const val ERROR_MESSAGE_JSON_PATH = "_embedded.errors[0].message"

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

    @JvmStatic
    fun invalidOwnerUpdates(): Stream<Arguments> =
        Stream.of(
            argumentSet(
                "Team name empty string",
                JSONObject()
                    .apply {
                        put(
                            "owner",
                            JSONObject().apply {
                                put("team", "")
                                put(
                                    "groups",
                                    listOf(
                                        "skip-stat-developers",
                                        "play-enhjoern-a-developers",
                                    ),
                                )
                            },
                        )
                    }.toString(),
                "can not be empty",
            ),
            argumentSet(
                "Team name null",
                JSONObject()
                    .apply {
                        put(
                            "owner",
                            JSONObject().apply {
                                put(
                                    "groups",
                                    listOf(
                                        "skip-stat-developers",
                                        "play-enhjoern-a-developers",
                                    ),
                                )
                            },
                        )
                    }.toString(),
                "can not be null",
            ),
            argumentSet(
                "Groups empty list",
                JSONObject()
                    .apply {
                        put(
                            "owner",
                            JSONObject().apply {
                                put("team", "skip-stat")
                            },
                        )
                    }.toString(),
                "can not be empty",
            ),
            argumentSet(
                "Groups empty values in list",
                JSONObject()
                    .apply {
                        put(
                            "owner",
                            JSONObject().apply {
                                put("team", "skip-stat")
                                put(
                                    "groups",
                                    listOf(
                                        "",
                                        "",
                                    ),
                                )
                            },
                        )
                    }.toString(),
                "can not be empty",
            ),
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
                "Error deserializing type",
            ),
            argumentSet(
                "external_reference_uri malformed uri",
                jsonTestInput()
                    .apply {
                        put(
                            "related_variable_definition_uris",
                            listOf("not a url"),
                        )
                    }.toString(),
                "Error deserializing type",
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

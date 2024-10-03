package no.ssb.metadata.vardef.utils

import io.micronaut.http.HttpStatus
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import java.util.stream.Stream

object TestUtils {
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
            // TODO: test case fails on update, will be fixed in DPMETA-498
//            argumentSet(
//                "short_name already exists",
//                JSONObject(
//                    jsonTestInput(),
//                ).apply { put("short_name", "intskatt") }.toString(),
//                "Short name intskatt already exists.",
//            ),
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
    fun variableDefinitionsNonMandatoryFieldsRemoved(): List<String> {
        val testCases =
            listOf(
                jsonTestInput()
                    .apply {
                        remove("measurement_type")
                    }.toString(),
                jsonTestInput()
                    .apply {
                        remove("valid_until")
                    }.toString(),
                jsonTestInput()
                    .apply {
                        remove("external_reference_uri")
                    }.toString(),
                jsonTestInput()
                    .apply {
                        remove("related_variable_definition_uris")
                    }.toString(),
            )
        return testCases
    }

    @JvmStatic
    fun variableDefinitionsMandatoryFieldsRemoved(): Stream<Arguments> {
        val testCases =
            listOf(
                jsonTestInput().apply {
                    remove("name")
                } to "null annotate it with @Nullable",
                jsonTestInput().apply {
                    remove("short_name")
                } to "null annotate it with @Nullable",
                jsonTestInput().apply {
                    remove("definition")
                } to "null annotate it with @Nullable",
                jsonTestInput().apply {
                    remove("valid_from")
                } to "null annotate it with @Nullable",
            )
        return testCases.stream().map { (json, message) -> Arguments.of(json.toString(), message) }
    }

    @JvmStatic
    fun variableDefinitionsVariousVariableStatus(): Stream<Arguments> {
        val testCases =
            listOf(
                jsonTestInput().apply {
                    put("variable_status", "DRAFT")
                } to HttpStatus.BAD_REQUEST.code,
                jsonTestInput().apply {
                    put("variable_status", "PUBLISHED_INTERNAL")
                } to HttpStatus.BAD_REQUEST.code,
                jsonTestInput().apply {
                    put("variable_status", "PUBLISHED_EXTERNAL")
                } to HttpStatus.BAD_REQUEST.code,
                jsonTestInput().apply {
                    put("variable_status", "DEPRECATED")
                } to HttpStatus.BAD_REQUEST.code,
                jsonTestInput().apply {
                    put("variable_status", "Not a status")
                } to HttpStatus.BAD_REQUEST.code,
            )

        return testCases.stream().map { (json, message) -> Arguments.of(json.toString(), message) }
    }
}

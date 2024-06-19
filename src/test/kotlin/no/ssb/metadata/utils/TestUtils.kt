

import io.micronaut.http.HttpStatus
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

object TestUtils {
    @JvmStatic
    fun invalidVariableDefinitions(): Stream<Arguments> {
        val testCases =
            listOf(
                JSONObject(JSON_TEST_INPUT).apply { put("valid_until", "2024-20-11") } to "Invalid date format",
                JSONObject(JSON_TEST_INPUT).apply { put("valid_from", "2024-20-11") } to "Invalid date format",
                JSONObject(JSON_TEST_INPUT).apply { put("external_reference_uri", "Not url") } to "Not url",
                JSONObject(JSON_TEST_INPUT).apply {
                    put(
                        "related_variable_definition_uris",
                        listOf("not a url", "https://example.com/", ""),
                    )
                } to "not a url",
                JSONObject(JSON_TEST_INPUT).apply {
                    getJSONObject("contact").put(
                        "email",
                        "not an email",
                    )
                } to "varDef.contact.email: must be a well-formed email address",
                JSONObject(JSON_TEST_INPUT).apply { remove("short_name") } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply { remove("name") } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply {
                    getJSONObject("name").apply {
                        remove("en")
                        put(
                            "se",
                            "Landbakgrunn",
                        )
                    }
                } to "Unknown property [se]",
                JSONObject(JSON_TEST_INPUT).apply {
                    put("id", "my-special-id")
                } to "ID may not be specified on creation.",
            )

        return testCases.stream().map { (json, message) -> Arguments.of(json.toString(), message) }
    }

    @JvmStatic
    fun variableDefinitionsNonMandatoryFieldsRemoved(): List<String> {
        val testCases =
            listOf(
                JSONObject(JSON_TEST_INPUT).apply { remove("measurement_type") }.toString(),
                JSONObject(JSON_TEST_INPUT).apply { remove("valid_until") }.toString(),
                JSONObject(JSON_TEST_INPUT).apply { remove("external_reference_uri") }.toString(),
                JSONObject(JSON_TEST_INPUT).apply { remove("related_variable_definition_uris") }.toString(),
            )
        return testCases
    }

    @JvmStatic
    fun variableDefinitionsMandatoryFieldsRemoved(): Stream<Arguments> {
        val testCases =
            listOf(
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("name")
                } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("short_name")
                } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("definition")
                } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("unit_types")
                } to "varDef.unitTypes: must not be empty",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("subject_fields")
                } to "varDef.subjectFields: must not be empty",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("variable_status")
                } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("valid_from")
                } to "null annotate it with @Nullable",
                JSONObject(JSON_TEST_INPUT).apply {
                    remove("contact")
                } to "null annotate it with @Nullable",
            )
        return testCases.stream().map { (json, message) -> Arguments.of(json.toString(), message) }
    }

    @JvmStatic
    fun variableDefinitionsVariousVariableStatus(): Stream<Arguments> {
        val testCases =
            listOf(
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "DRAFT")
                } to HttpStatus.CREATED.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "PUBLISHED_INTERNAL")
                } to HttpStatus.CREATED.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "PUBLISHED_EXTERNAL")
                } to HttpStatus.CREATED.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "DEPRECATED")
                } to HttpStatus.CREATED.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "Not a status")
                } to HttpStatus.INTERNAL_SERVER_ERROR.code,
            )

        return testCases.stream().map { (json, message) -> Arguments.of(json.toString(), message) }
    }
}

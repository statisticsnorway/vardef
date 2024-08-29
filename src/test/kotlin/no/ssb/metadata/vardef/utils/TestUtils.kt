package no.ssb.metadata.vardef.utils

import io.micronaut.http.HttpStatus
import no.ssb.metadata.vardef.models.LanguageStringType
import org.json.JSONObject
import org.junit.jupiter.params.provider.Arguments
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
    fun invalidVariableDefinitions(): Stream<Arguments> {
        val testCases =
            listOf(
                JSONObject(JSON_TEST_INPUT).apply {
                    getJSONObject("name").apply {
                        remove("en")
                        put(
                            "se",
                            "Landbakgrunn",
                        )
                    }
                } to "Unknown property [se]",
                JSONObject(
                    JSON_TEST_INPUT,
                ).apply { put("unit_types", listOf("blah")) } to "Code blah is not a member of classification with id",
                JSONObject(
                    JSON_TEST_INPUT,
                ).apply {
                    put(
                        "subject_fields",
                        listOf("blah"),
                    )
                } to "Code blah is not a member of classification with id",
                JSONObject(
                    JSON_TEST_INPUT,
                ).apply { put("measurement_type", "blah") } to "Code blah is not a member of classification with id",
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
                } to "must be a well-formed email address",
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
                    remove("valid_from")
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
                } to HttpStatus.BAD_REQUEST.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "PUBLISHED_INTERNAL")
                } to HttpStatus.BAD_REQUEST.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "PUBLISHED_EXTERNAL")
                } to HttpStatus.BAD_REQUEST.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "DEPRECATED")
                } to HttpStatus.BAD_REQUEST.code,
                JSONObject(JSON_TEST_INPUT).apply {
                    put("variable_status", "Not a status")
                } to HttpStatus.BAD_REQUEST.code,
            )

        return testCases.stream().map { (json, message) -> Arguments.of(json.toString(), message) }
    }

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

    @JvmStatic
    fun provideTestDataCheckDefinition(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "For personer født",
                            nn = "For personer født",
                            en = "Country background is",
                        ),
                ),
                false,
            ),
            Arguments.of(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "For personer født i går",
                            nn = "For personer født i går",
                            en = "Persons born yesterday",
                        ),
                ),
                true,
            ),
            Arguments.of(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "For personer født",
                            nn = "For personer født",
                            en = "Country background is",
                        ),
                ),
                false,
            ),
            Arguments.of(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "For personer født i går",
                            nn = "For personer født",
                            en = "Country background is",
                        ),
                ),
                false,
            ),
            Arguments.of(
                INPUT_VARIABLE_DEFINITION.copy(
                    definition =
                        LanguageStringType(
                            nb = "Hester og kuer født",
                            nn = "Hester og kuer født",
                            en = "Horses and cows born",
                        ),
                ),
                true,
            ),
        )
    }
}

package no.ssb.metadata.vardef.models

import io.micronaut.core.type.Argument
import io.micronaut.json.JsonMapper
import io.micronaut.json.tree.JsonNode
import no.ssb.metadata.vardef.extensions.fieldNames
import no.ssb.metadata.vardef.extensions.has
import java.net.URL
import java.time.LocalDate

/**
 * Input class which allows us to detect the following three cases:
 * - Undefined (field missing): keep existing value
 * - Present(value): set value
 * - Present(null): clear value for nullable fields
 */
data class UpdateDraftInput(
    val name: FieldPresence<LanguageStringType> = FieldPresence.Undefined,
    val shortName: FieldPresence<String> = FieldPresence.Undefined,
    val definition: FieldPresence<LanguageStringType> = FieldPresence.Undefined,
    val classificationReference: FieldPresence<String?> = FieldPresence.Undefined,
    val unitTypes: FieldPresence<List<String>> = FieldPresence.Undefined,
    val subjectFields: FieldPresence<List<String>> = FieldPresence.Undefined,
    val containsSpecialCategoriesOfPersonalData: FieldPresence<Boolean> = FieldPresence.Undefined,
    val variableStatus: FieldPresence<VariableStatus> = FieldPresence.Undefined,
    val measurementType: FieldPresence<String?> = FieldPresence.Undefined,
    val validFrom: FieldPresence<LocalDate> = FieldPresence.Undefined,
    val validUntil: FieldPresence<LocalDate?> = FieldPresence.Undefined,
    val externalReferenceUri: FieldPresence<URL?> = FieldPresence.Undefined,
    val comment: FieldPresence<LanguageStringType?> = FieldPresence.Undefined,
    val relatedVariableDefinitionUris: FieldPresence<List<URL>?> = FieldPresence.Undefined,
    val owner: FieldPresence<Owner> = FieldPresence.Undefined,
    val contact: FieldPresence<Contact> = FieldPresence.Undefined,
) {
    fun toUpdateDraft(): UpdateDraft =
        UpdateDraft(
            name = name.definedValueOrNull(),
            shortName = shortName.definedValueOrNull(),
            definition = definition.definedValueOrNull(),
            classificationReference = classificationReference.definedValueOrNull(),
            unitTypes = unitTypes.definedValueOrNull(),
            subjectFields = subjectFields.definedValueOrNull(),
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData.definedValueOrNull(),
            variableStatus = variableStatus.definedValueOrNull(),
            measurementType = measurementType.definedValueOrNull(),
            validFrom = validFrom.definedValueOrNull(),
            validUntil = validUntil.definedValueOrNull(),
            externalReferenceUri = externalReferenceUri.definedValueOrNull(),
            comment = comment.definedValueOrNull(),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris.definedValueOrNull(),
            owner = owner.definedValueOrNull(),
            contact = contact.definedValueOrNull(),
        )

    companion object {
        private val allowedFields =
            setOf(
                "name",
                "short_name",
                "definition",
                "classification_reference",
                "unit_types",
                "subject_fields",
                "contains_special_categories_of_personal_data",
                "variable_status",
                "measurement_type",
                "valid_from",
                "valid_until",
                "external_reference_uri",
                "comment",
                "related_variable_definition_uris",
                "owner",
                "contact",
            )

        fun fromJson(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): UpdateDraftInput {
            require(root.isObject) { "Request body must be a JSON object" }

            root.fieldNames().forEach { fieldName ->
                require(fieldName in allowedFields) {
                    "Unknown property [$fieldName] encountered during deserialization of type ${UpdateDraft::class.qualifiedName}"
                }
            }

            validateLanguageFields(root)

            return runCatching {
                UpdateDraftInput(
                    name = requiredValue(root, "name", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    shortName = requiredValue(root, "short_name", jsonMapper, Argument.of(String::class.java)),
                    definition = requiredValue(root, "definition", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    classificationReference = nullableValue(root, "classification_reference", jsonMapper, Argument.of(String::class.java)),
                    unitTypes = requiredValue(root, "unit_types", jsonMapper, Argument.listOf(String::class.java)),
                    subjectFields = requiredValue(root, "subject_fields", jsonMapper, Argument.listOf(String::class.java)),
                    containsSpecialCategoriesOfPersonalData =
                        requiredValue(root, "contains_special_categories_of_personal_data", jsonMapper, Argument.of(Boolean::class.java)),
                    variableStatus = requiredValue(root, "variable_status", jsonMapper, Argument.of(VariableStatus::class.java)),
                    measurementType = nullableValue(root, "measurement_type", jsonMapper, Argument.of(String::class.java)),
                    validFrom = validFromValue(root, jsonMapper),
                    validUntil = nullableValue(root, "valid_until", jsonMapper, Argument.of(LocalDate::class.java)),
                    externalReferenceUri = nullableValue(root, "external_reference_uri", jsonMapper, Argument.of(URL::class.java)),
                    comment = nullableValue(root, "comment", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    relatedVariableDefinitionUris =
                        nullableValue(root, "related_variable_definition_uris", jsonMapper, Argument.listOf(URL::class.java)),
                    owner = ownerValue(root, jsonMapper),
                    contact = requiredValue(root, "contact", jsonMapper, Argument.of(Contact::class.java)),
                )
            }.getOrElse { error ->
                throw IllegalArgumentException(error.message ?: "Invalid JSON")
            }
        }

        private fun <T : Any> requiredValue(
            root: JsonNode,
            fieldName: String,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): FieldPresence<T> {
            val node = root.get(fieldName) ?: return FieldPresence.Undefined
            if (node.isNull) {
                throw IllegalArgumentException("$fieldName can not be null")
            }
            return FieldPresence.Present(decode(node, jsonMapper, argument))
        }

        private fun <T : Any> nullableValue(
            root: JsonNode,
            fieldName: String,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): FieldPresence<T?> {
            val node = root.get(fieldName) ?: return FieldPresence.Undefined
            if (node.isNull) return FieldPresence.Present(null)
            return FieldPresence.Present(decode(node, jsonMapper, argument))
        }

        private fun validFromValue(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): FieldPresence<LocalDate> {
            val node = root.get("valid_from") ?: return FieldPresence.Undefined
            if (node.isNull) throw IllegalArgumentException("valid_from can not be null")
            return runCatching {
                FieldPresence.Present(decode(node, jsonMapper, Argument.of(LocalDate::class.java)))
            }.getOrElse {
                throw IllegalArgumentException("Error decoding property [LocalDate validFrom]")
            }
        }

        private fun ownerValue(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): FieldPresence<Owner> {
            val node = root.get("owner") ?: return FieldPresence.Undefined
            if (node.isNull) throw IllegalArgumentException("owner can not be null")
            if (node.isObject && !node.has("team")) {
                throw IllegalArgumentException("owner team and groups can not be null")
            }
            if (node.isObject && !node.has("groups")) {
                val team =
                    decode(
                        node.get("team") ?: throw IllegalArgumentException("owner team can not be null"),
                        jsonMapper,
                        Argument.of(String::class.java),
                    )
                return FieldPresence.Present(Owner(team = team, groups = emptyList()))
            }
            return FieldPresence.Present(decode(node, jsonMapper, Argument.of(Owner::class.java)))
        }

        private fun validateLanguageFields(root: JsonNode) {
            listOf("name", "definition", "comment").forEach { fieldName ->
                val node = root.get(fieldName) ?: return@forEach
                if (!node.isObject) return@forEach
                node.fieldNames().forEach { languageField ->
                    require(languageField in SupportedLanguages.toSet()) {
                        "Unknown property [$languageField] encountered during deserialization of type ${LanguageStringType::class.qualifiedName} in field [$fieldName]"
                    }
                }
            }
        }

        private fun <T : Any> decode(
            node: JsonNode,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): T = jsonMapper.readValueFromTree(node, argument)!!
    }
}

package no.ssb.metadata.vardef.models

import com.fasterxml.jackson.databind.JsonNode
import io.micronaut.core.type.Argument
import io.micronaut.json.JsonMapper
import java.net.URL
import java.time.LocalDate

/**
 * PATCH payload where each top-level field has tri-state semantics:
 * - Undefined (field missing): keep existing value
 * - Present(value): set value
 * - Present(null): clear value for nullable fields
 */
data class UpdateDraftPatch(
    val name: PatchField<LanguageStringType> = PatchField.Undefined,
    val shortName: PatchField<String> = PatchField.Undefined,
    val definition: PatchField<LanguageStringType> = PatchField.Undefined,
    val classificationReference: PatchField<String?> = PatchField.Undefined,
    val unitTypes: PatchField<List<String>> = PatchField.Undefined,
    val subjectFields: PatchField<List<String>> = PatchField.Undefined,
    val containsSpecialCategoriesOfPersonalData: PatchField<Boolean> = PatchField.Undefined,
    val variableStatus: PatchField<VariableStatus> = PatchField.Undefined,
    val measurementType: PatchField<String?> = PatchField.Undefined,
    val validFrom: PatchField<LocalDate> = PatchField.Undefined,
    val validUntil: PatchField<LocalDate?> = PatchField.Undefined,
    val externalReferenceUri: PatchField<URL?> = PatchField.Undefined,
    val comment: PatchField<LanguageStringType?> = PatchField.Undefined,
    val relatedVariableDefinitionUris: PatchField<List<URL>?> = PatchField.Undefined,
    val owner: PatchField<Owner> = PatchField.Undefined,
    val contact: PatchField<Contact> = PatchField.Undefined,
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
        ): UpdateDraftPatch {
            require(root.isObject) { "Request body must be a JSON object" }

            root.fieldNames().forEachRemaining { fieldName ->
                require(fieldName in allowedFields) {
                    "Unknown property [$fieldName] encountered during deserialization of type ${UpdateDraft::class.qualifiedName}"
                }
            }

            validateLanguageFields(root)

            return runCatching {
                UpdateDraftPatch(
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

        private fun <T> requiredValue(
            root: JsonNode,
            fieldName: String,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): PatchField<T> {
            val node = root.get(fieldName) ?: return PatchField.Undefined
            // Keep previous PATCH behavior when payloads include explicit null for optional fields.
            if (node.isNull) return PatchField.Undefined
            return PatchField.Present(decode(node, jsonMapper, argument))
        }

        private fun <T> nullableValue(
            root: JsonNode,
            fieldName: String,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): PatchField<T?> {
            val node = root.get(fieldName) ?: return PatchField.Undefined
            if (node.isNull) return PatchField.Present(null)
            return PatchField.Present(decode(node, jsonMapper, argument))
        }

        private fun validFromValue(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): PatchField<LocalDate> {
            val node = root.get("valid_from") ?: return PatchField.Undefined
            if (node.isNull) return PatchField.Undefined
            return runCatching {
                PatchField.Present(decode(node, jsonMapper, Argument.of(LocalDate::class.java)))
            }.getOrElse {
                throw IllegalArgumentException("Error decoding property [LocalDate validFrom]")
            }
        }

        private fun ownerValue(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): PatchField<Owner> {
            val node = root.get("owner") ?: return PatchField.Undefined
            if (node.isNull) return PatchField.Undefined
            if (node.isObject && !node.has("team")) {
                throw IllegalArgumentException("owner team and groups can not be null")
            }
            if (node.isObject && !node.has("groups")) {
                val team = decode(node.get("team"), jsonMapper, Argument.of(String::class.java))
                return PatchField.Present(Owner(team = team, groups = emptyList()))
            }
            return PatchField.Present(decode(node, jsonMapper, Argument.of(Owner::class.java)))
        }

        private fun validateLanguageFields(root: JsonNode) {
            val allowedLanguages = setOf("nb", "nn", "en")
            listOf("name", "definition", "comment").forEach { fieldName ->
                val node = root.get(fieldName) ?: return@forEach
                if (!node.isObject) return@forEach
                node.fieldNames().forEachRemaining { languageField ->
                    require(languageField in allowedLanguages) {
                        "Unknown property [$languageField] encountered during deserialization of type ${LanguageStringType::class.qualifiedName} in field [$fieldName]"
                    }
                }
            }
        }

        private fun <T> decode(
            node: JsonNode,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): T = jsonMapper.readValue(node.toString(), argument)
    }
}


package no.ssb.metadata.vardef.models

import com.fasterxml.jackson.databind.JsonNode
import io.micronaut.core.type.Argument
import io.micronaut.json.JsonMapper
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Input payload for create-validity-period endpoint with tri-state semantics.
 */
data class CreateValidityPeriodInput(
    val name: FieldPresence<LanguageStringType> = FieldPresence.Undefined,
    val definition: FieldPresence<LanguageStringType> = FieldPresence.Undefined,
    val classificationReference: FieldPresence<String?> = FieldPresence.Undefined,
    val unitTypes: FieldPresence<List<String>> = FieldPresence.Undefined,
    val subjectFields: FieldPresence<List<String>> = FieldPresence.Undefined,
    val containsSpecialCategoriesOfPersonalData: FieldPresence<Boolean> = FieldPresence.Undefined,
    val measurementType: FieldPresence<String?> = FieldPresence.Undefined,
    val validFrom: FieldPresence<LocalDate> = FieldPresence.Undefined,
    val externalReferenceUri: FieldPresence<URL?> = FieldPresence.Undefined,
    val comment: FieldPresence<LanguageStringType?> = FieldPresence.Present(null),
    val relatedVariableDefinitionUris: FieldPresence<List<URL>?> = FieldPresence.Present(null),
    val contact: FieldPresence<Contact> = FieldPresence.Undefined,
) {
    fun toCreateValidityPeriod(): CreateValidityPeriod =
        CreateValidityPeriod(
            name = name.definedValueOrNull(),
            definition = definition.requiredValue(),
            classificationReference = classificationReference.definedValueOrNull(),
            unitTypes = unitTypes.definedValueOrNull(),
            subjectFields = subjectFields.definedValueOrNull(),
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData.definedValueOrNull(),
            measurementType = measurementType.definedValueOrNull(),
            validFrom = validFrom.requiredValue(),
            externalReferenceUri = externalReferenceUri.definedValueOrNull(),
            comment = comment.definedValueOrNull(),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris.definedValueOrNull(),
            contact = contact.definedValueOrNull(),
        )

    fun toSavedVariableDefinition(
        highestPatchId: Int,
        previousPatch: SavedVariableDefinition,
        userName: String,
    ): SavedVariableDefinition =
        previousPatch.copy(
            patchId = highestPatchId + 1,
            name =
                when (val updates = name) {
                    FieldPresence.Undefined -> previousPatch.name
                    is FieldPresence.Present -> previousPatch.name.update(updates.value)
                },
            definition = definition.requiredValue(),
            classificationReference = classificationReference.applyNullable(previousPatch.classificationReference),
            unitTypes = unitTypes.orElse(previousPatch.unitTypes),
            subjectFields = subjectFields.orElse(previousPatch.subjectFields),
            containsSpecialCategoriesOfPersonalData =
                containsSpecialCategoriesOfPersonalData.orElse(previousPatch.containsSpecialCategoriesOfPersonalData),
            variableStatus = previousPatch.variableStatus,
            measurementType = measurementType.applyNullable(previousPatch.measurementType),
            validFrom = validFrom.requiredValue(),
            externalReferenceUri = externalReferenceUri.applyNullable(previousPatch.externalReferenceUri),
            comment = comment.applyNullable(previousPatch.comment),
            relatedVariableDefinitionUris =
                when (val updates = relatedVariableDefinitionUris) {
                    FieldPresence.Undefined -> previousPatch.relatedVariableDefinitionUris
                    is FieldPresence.Present -> updates.value?.map { it.toString() }
                },
            contact = contact.orElse(previousPatch.contact),
            // Placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            lastUpdatedBy = userName,
        )

    companion object {
        private val allowedFields =
            setOf(
                "name",
                "definition",
                "classification_reference",
                "unit_types",
                "subject_fields",
                "contains_special_categories_of_personal_data",
                "measurement_type",
                "valid_from",
                "external_reference_uri",
                "comment",
                "related_variable_definition_uris",
                "contact",
            )

        fun fromJson(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): CreateValidityPeriodInput {
            require(root.isObject) { "Request body must be a JSON object" }

            root.fieldNames().forEachRemaining { fieldName ->
                require(fieldName in allowedFields) {
                    when (fieldName) {
                        "short_name" -> {
                            "short_name may not be specified here"
                        }

                        "valid_until" -> {
                            "valid_until may not be specified here"
                        }

                        else -> {
                            "Unknown property [$fieldName] encountered during deserialization of type ${CreateValidityPeriod::class.qualifiedName}"
                        }
                    }
                }
            }

            validateLanguageFields(root)

            return runCatching {
                CreateValidityPeriodInput(
                    name = requiredValue(root, "name", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    definition = requiredValue(root, "definition", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    classificationReference = nullableValue(root, "classification_reference", jsonMapper, Argument.of(String::class.java)),
                    unitTypes = requiredValue(root, "unit_types", jsonMapper, Argument.listOf(String::class.java)),
                    subjectFields = requiredValue(root, "subject_fields", jsonMapper, Argument.listOf(String::class.java)),
                    containsSpecialCategoriesOfPersonalData =
                        requiredValue(root, "contains_special_categories_of_personal_data", jsonMapper, Argument.of(Boolean::class.java)),
                    measurementType = nullableValue(root, "measurement_type", jsonMapper, Argument.of(String::class.java)),
                    validFrom = validFromValue(root, jsonMapper),
                    externalReferenceUri = nullableValue(root, "external_reference_uri", jsonMapper, Argument.of(URL::class.java)),
                    // Preserve existing endpoint behavior where omitted values clear these fields.
                    comment = nullableValueWithDefaultNull(root, "comment", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    relatedVariableDefinitionUris =
                        nullableValueWithDefaultNull(
                            root,
                            "related_variable_definition_uris",
                            jsonMapper,
                            Argument.listOf(URL::class.java),
                        ),
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

        private fun <T : Any> nullableValueWithDefaultNull(
            root: JsonNode,
            fieldName: String,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): FieldPresence<T?> {
            val node = root.get(fieldName) ?: return FieldPresence.Present(null)
            if (node.isNull) return FieldPresence.Present(null)
            return FieldPresence.Present(decode(node, jsonMapper, argument))
        }

        private fun validFromValue(
            root: JsonNode,
            jsonMapper: JsonMapper,
        ): FieldPresence<LocalDate> {
            val node = root.get("valid_from") ?: return FieldPresence.Undefined
            if (node.isNull) return FieldPresence.Undefined
            return runCatching {
                FieldPresence.Present(decode(node, jsonMapper, Argument.of(LocalDate::class.java)))
            }.getOrElse {
                throw IllegalArgumentException("Error decoding property [LocalDate validFrom]")
            }
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

        private fun <T : Any> decode(
            node: JsonNode,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): T = jsonMapper.readValue(node.toString(), argument)!!
    }
}

private fun <T> FieldPresence<T>.requiredValue(): T =
    when (this) {
        FieldPresence.Undefined -> {
            throw IllegalArgumentException("Value can not be null")
        }

        is FieldPresence.Present -> {
            value ?: throw IllegalArgumentException("Value can not be null")
        }
    }

package no.ssb.metadata.vardef.models

import com.fasterxml.jackson.databind.JsonNode
import io.micronaut.core.type.Argument
import io.micronaut.json.JsonMapper
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * PATCH payload for create-patch endpoint with tri-state semantics for nullable fields.
 */
data class CreatePatchPatch(
    val name: PatchField<LanguageStringType> = PatchField.Undefined,
    val definition: PatchField<LanguageStringType> = PatchField.Undefined,
    val classificationReference: PatchField<String?> = PatchField.Undefined,
    val unitTypes: PatchField<List<String>> = PatchField.Undefined,
    val subjectFields: PatchField<List<String>> = PatchField.Undefined,
    val containsSpecialCategoriesOfPersonalData: PatchField<Boolean> = PatchField.Undefined,
    val variableStatus: PatchField<VariableStatus> = PatchField.Undefined,
    val measurementType: PatchField<String?> = PatchField.Undefined,
    val validUntil: PatchField<LocalDate?> = PatchField.Undefined,
    val externalReferenceUri: PatchField<URL?> = PatchField.Undefined,
    val comment: PatchField<LanguageStringType?> = PatchField.Undefined,
    val relatedVariableDefinitionUris: PatchField<List<URL>?> = PatchField.Undefined,
    val owner: PatchField<Owner> = PatchField.Undefined,
    val contact: PatchField<Contact> = PatchField.Undefined,
) {
    fun toCreatePatch(): CreatePatch =
        CreatePatch(
            name = name.definedValueOrNull(),
            definition = definition.definedValueOrNull(),
            classificationReference = classificationReference.definedValueOrNull(),
            unitTypes = unitTypes.definedValueOrNull(),
            subjectFields = subjectFields.definedValueOrNull(),
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData.definedValueOrNull(),
            variableStatus = variableStatus.definedValueOrNull(),
            measurementType = measurementType.definedValueOrNull(),
            validUntil = validUntil.definedValueOrNull(),
            externalReferenceUri = externalReferenceUri.definedValueOrNull(),
            comment = comment.definedValueOrNull(),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris.definedValueOrNull(),
            owner = owner.definedValueOrNull(),
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
                    PatchField.Undefined -> previousPatch.name
                    is PatchField.Present -> previousPatch.name.update(updates.value)
                },
            definition =
                when (val updates = definition) {
                    PatchField.Undefined -> previousPatch.definition
                    is PatchField.Present -> previousPatch.definition.update(updates.value)
                },
            classificationReference = classificationReference.applyNullable(previousPatch.classificationReference),
            unitTypes = unitTypes.orElse(previousPatch.unitTypes),
            subjectFields = subjectFields.orElse(previousPatch.subjectFields),
            containsSpecialCategoriesOfPersonalData =
                containsSpecialCategoriesOfPersonalData.orElse(previousPatch.containsSpecialCategoriesOfPersonalData),
            variableStatus = variableStatus.orElse(previousPatch.variableStatus),
            measurementType = measurementType.applyNullable(previousPatch.measurementType),
            validUntil = validUntil.applyNullable(previousPatch.validUntil),
            externalReferenceUri = externalReferenceUri.applyNullable(previousPatch.externalReferenceUri),
            comment =
                when (val updates = comment) {
                    PatchField.Undefined -> previousPatch.comment
                    is PatchField.Present -> updates.value?.let { previousPatch.comment?.update(it) ?: it }
                },
            relatedVariableDefinitionUris =
                when (val updates = relatedVariableDefinitionUris) {
                    PatchField.Undefined -> previousPatch.relatedVariableDefinitionUris
                    is PatchField.Present -> updates.value?.map { it.toString() }
                },
            owner = owner.orElse(previousPatch.owner),
            contact = contact.orElse(previousPatch.contact),
            // Placeholder values, actual timestamps are set by persistence annotations.
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
                "variable_status",
                "measurement_type",
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
        ): CreatePatchPatch {
            require(root.isObject) { "Request body must be a JSON object" }

            root.fieldNames().forEachRemaining { fieldName ->
                require(fieldName in allowedFields) {
                    when (fieldName) {
                        "short_name" -> "short_name may not be specified here"
                        "valid_from" -> "valid_from may not be specified here"
                        else ->
                            "Unknown property [$fieldName] encountered during deserialization of type ${CreatePatch::class.qualifiedName}"
                    }
                }
            }

            validateLanguageFields(root)

            return runCatching {
                CreatePatchPatch(
                    name = requiredValue(root, "name", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    definition = requiredValue(root, "definition", jsonMapper, Argument.of(LanguageStringType::class.java)),
                    classificationReference = nullableValue(root, "classification_reference", jsonMapper, Argument.of(String::class.java)),
                    unitTypes = requiredValue(root, "unit_types", jsonMapper, Argument.listOf(String::class.java)),
                    subjectFields = requiredValue(root, "subject_fields", jsonMapper, Argument.listOf(String::class.java)),
                    containsSpecialCategoriesOfPersonalData =
                        requiredValue(root, "contains_special_categories_of_personal_data", jsonMapper, Argument.of(Boolean::class.java)),
                    variableStatus = requiredValue(root, "variable_status", jsonMapper, Argument.of(VariableStatus::class.java)),
                    measurementType = nullableValue(root, "measurement_type", jsonMapper, Argument.of(String::class.java)),
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
        ): PatchField<T> {
            val node = root.get(fieldName) ?: return PatchField.Undefined
            if (node.isNull) return PatchField.Undefined
            return PatchField.Present(decode(node, jsonMapper, argument))
        }

        private fun <T : Any> nullableValue(
            root: JsonNode,
            fieldName: String,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): PatchField<T?> {
            val node = root.get(fieldName) ?: return PatchField.Undefined
            if (node.isNull) return PatchField.Present(null)
            return PatchField.Present(decode(node, jsonMapper, argument))
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

        private fun <T : Any> decode(
            node: JsonNode,
            jsonMapper: JsonMapper,
            argument: Argument<T>,
        ): T = jsonMapper.readValue(node.toString(), argument)!!
    }
}



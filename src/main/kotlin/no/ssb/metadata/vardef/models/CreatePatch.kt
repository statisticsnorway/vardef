package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import no.ssb.metadata.vardef.annotations.KlassCode
import no.ssb.metadata.vardef.annotations.KlassCodeAtLevel
import no.ssb.metadata.vardef.annotations.KlassId
import no.ssb.metadata.vardef.annotations.NotEmptyLanguageStringType
import no.ssb.metadata.vardef.constants.*
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Create a new Patch version on a Published Variable Definition.
 */
@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = PATCH_EXAMPLE,
)
data class CreatePatch(
    @Nullable
    @NotEmptyLanguageStringType
    val name: LanguageStringType? = null,
    @Nullable
    @NotEmptyLanguageStringType
    val definition: LanguageStringType? = null,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String? = null,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    @Nullable
    val unitTypes: List<@KlassCode(UNIT_TYPES_KLASS_CODE) @NotEmpty String>? = null,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @Nullable
    val subjectFields: List<@KlassCode(SUBJECT_FIELDS_KLASS_CODE) @NotEmpty String>? = null,
    @Schema(description = CONTAINS_SPECIAL_CATEGORIES_OF_PERSONAL_DATA_FIELD_DESCRIPTION)
    @Nullable
    val containsSpecialCategoriesOfPersonalData: Boolean? = null,
    @Nullable
    val variableStatus: VariableStatus? = null,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCodeAtLevel(MEASUREMENT_TYPE_KLASS_CODE, MEASUREMENT_TYPE_KLASS_LEVEL)
    val measurementType: String? = null,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @Format(DATE_FORMAT)
    val validUntil: LocalDate? = null,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    val externalReferenceUri: URL? = null,
    @Nullable
    @NotEmptyLanguageStringType
    val comment: LanguageStringType? = null,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<URL>? = null,
    @Nullable
    @Valid
    val owner: Owner? = null,
    @Valid
    @Nullable
    val contact: Contact? = null,
) {
    fun toSavedVariableDefinition(
        highestPatchId: Int,
        previousPatch: SavedVariableDefinition,
        userName: String,
    ): SavedVariableDefinition =
        previousPatch.copy(
            patchId = highestPatchId + 1,
            name = name?.let { previousPatch.name.update(it) } ?: previousPatch.name,
            definition = definition?.let { previousPatch.definition.update(definition) } ?: previousPatch.definition,
            classificationReference = classificationReference ?: previousPatch.classificationReference,
            unitTypes = unitTypes ?: previousPatch.unitTypes,
            subjectFields = subjectFields ?: previousPatch.subjectFields,
            containsSpecialCategoriesOfPersonalData =
                containsSpecialCategoriesOfPersonalData ?: previousPatch.containsSpecialCategoriesOfPersonalData,
            variableStatus = variableStatus ?: previousPatch.variableStatus,
            measurementType = measurementType ?: previousPatch.measurementType,
            validUntil = validUntil ?: previousPatch.validUntil,
            externalReferenceUri = externalReferenceUri ?: previousPatch.externalReferenceUri,
            comment = comment?.let { previousPatch.comment?.update(comment) } ?: previousPatch.comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            owner = owner ?: previousPatch.owner,
            contact = contact ?: previousPatch.contact,
            // Provide a placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            lastUpdatedBy = userName,
        )
}

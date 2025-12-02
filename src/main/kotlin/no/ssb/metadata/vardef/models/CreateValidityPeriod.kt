package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import no.ssb.metadata.vardef.annotations.KlassCode
import no.ssb.metadata.vardef.annotations.KlassId
import no.ssb.metadata.vardef.annotations.NotEmptyLanguageStringType
import no.ssb.metadata.vardef.constants.*
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Create a new Validity Period on a Published Variable Definition.
 */
@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = CREATE_VALIDITY_PERIOD_EXAMPLE,
)
data class CreateValidityPeriod(
    @Schema(description = NAME_FIELD_DESCRIPTION)
    @Nullable
    @NotEmptyLanguageStringType
    val name: LanguageStringType?,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    @NotNull
    @NotEmptyLanguageStringType
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    @Nullable
    val unitTypes: List<@KlassCode(id = UNIT_TYPES_KLASS_CODE) @NotEmpty String>?,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @Nullable
    val subjectFields: List<@KlassCode(id = SUBJECT_FIELDS_KLASS_CODE) @NotEmpty String>?,
    @Schema(description = CONTAINS_SPECIAL_CATEGORIES_OF_PERSONAL_DATA_FIELD_DESCRIPTION)
    @Nullable
    val containsSpecialCategoriesOfPersonalData: Boolean?,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode(MEASUREMENT_TYPE_KLASS_CODE)
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Format(DATE_FORMAT)
    @NotNull
    val validFrom: LocalDate,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    val externalReferenceUri: URL?,
    @Schema(description = COMMENT_FIELD_DESCRIPTION)
    @Nullable
    @NotEmptyLanguageStringType
    val comment: LanguageStringType?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<URL>?,
    @Schema(description = CONTACT_FIELD_DESCRIPTION)
    @Valid
    @Nullable
    val contact: Contact?,
) {
    fun toSavedVariableDefinition(
        highestPatchId: Int,
        previousPatch: SavedVariableDefinition,
        userName: String,
    ): SavedVariableDefinition =
        previousPatch.copy(
            patchId = highestPatchId + 1,
            name = name?.let { previousPatch.name.update(it) } ?: previousPatch.name,
            definition = definition,
            classificationReference = classificationReference ?: previousPatch.classificationReference,
            unitTypes = unitTypes ?: previousPatch.unitTypes,
            subjectFields = subjectFields ?: previousPatch.subjectFields,
            containsSpecialCategoriesOfPersonalData =
            containsSpecialCategoriesOfPersonalData ?: previousPatch.containsSpecialCategoriesOfPersonalData,
            variableStatus = previousPatch.variableStatus,
            measurementType = measurementType ?: previousPatch.measurementType,
            validFrom = validFrom,
            externalReferenceUri = externalReferenceUri ?: previousPatch.externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            contact = contact ?: previousPatch.contact,
            // Provide a placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            lastUpdatedBy = userName,
        )
}

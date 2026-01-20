package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.vardef.annotations.*
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.services.VariableDefinitionService
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Create a Draft Variable Definition
 */
@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = CREATE_DRAFT_EXAMPLE,
)
@ValidDateOrder
data class CreateDraft(
    @NotEmptyLanguageStringType
    val name: LanguageStringType,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = VARDEF_SHORT_NAME_PATTERN)
    val shortName: String,
    @NotEmptyLanguageStringType
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<@KlassCode(id = UNIT_TYPES_KLASS_CODE) @NotEmpty String>,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    val subjectFields: List<@KlassCode(id = SUBJECT_FIELDS_KLASS_CODE) @NotEmpty String>,
    @Schema(description = CONTAINS_SPECIAL_CATEGORIES_OF_PERSONAL_DATA_FIELD_DESCRIPTION, defaultValue = false.toString())
    val containsSpecialCategoriesOfPersonalData: Boolean = false,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCodeAtLevel(MEASUREMENT_TYPE_KLASS_CODE, MEASUREMENT_TYPE_KLASS_LEVEL)
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Format(DATE_FORMAT)
    val validFrom: LocalDate,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @Format(DATE_FORMAT)
    val validUntil: LocalDate?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    val externalReferenceUri: URL?,
    @Nullable
    @NotEmptyLanguageStringType
    val comment: LanguageStringType?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<URL>?,
    @Valid
    val contact: Contact,
) {
    /**
     * Team name is a substring of group name
     */
    private fun parseTeamName(ownerGroup: String): String {
        // When group name ends with 'data-admins' it is a special case
        return if (ownerGroup.endsWith("data-admins")) {
            ownerGroup.substringBeforeLast("-").substringBeforeLast("-")
        } else {
            ownerGroup.substringBeforeLast("-")
        }
    }

    fun toSavedVariableDefinition(
        ownerGroup: String,
        userName: String,
    ): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = VariableDefinitionService.generateId(),
            patchId = 1,
            name = name,
            shortName = shortName,
            definition = definition,
            classificationReference = classificationReference,
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData,
            variableStatus = VariableStatus.DRAFT,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            owner = Owner(parseTeamName(ownerGroup), listOf(ownerGroup)),
            contact = contact,
            // Provide a placeholder value, actual value set by data layer
            createdAt = LocalDateTime.now(),
            createdBy = userName,
            // Provide a placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            lastUpdatedBy = userName,
        )
}

package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.klass.service.KlassService
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Complete view
 *
 * For internal users who need all details while maintaining variable definitions.
 */
@Serdeable(naming = SnakeCaseStrategy::class)
data class CompleteView(
    @Schema(description = ID_FIELD_DESCRIPTION, format = VARDEF_ID_PATTERN)
    var id: String,
    @Schema(description = PATCH_ID_FIELD_DESCRIPTION, example = "1")
    var patchId: Int,
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<String>,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    val subjectFields: List<String>,
    @Schema(description = CONTAINS_SPECIAL_CATEGORIES_OF_PERSONAL_DATA_FIELD_DESCRIPTION)
    val containsSpecialCategoriesOfPersonalData: Boolean,
    @Schema(
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
    )
    var variableStatus: VariableStatus?,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    val validFrom: LocalDate,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    val validUntil: LocalDate?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    val externalReferenceUri: URL?,
    @Schema(description = COMMENT_FIELD_DESCRIPTION)
    val comment: LanguageStringType?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    val relatedVariableDefinitionUris: List<URL>?,
    @Schema(description = OWNER_DESCRIPTION)
    val owner: Owner,
    @Schema(description = CONTACT_FIELD_DESCRIPTION)
    val contact: Contact,
    @Schema(description = CREATED_AT_FIELD_DESCRIPTION)
    var createdAt: LocalDateTime,
    @Schema(description = CREATED_BY_FIELD_DESCRIPTION)
    var createdBy: String,
    @Schema(description = LAST_UPDATED_AT_FIELD_DESCRIPTION)
    var lastUpdatedAt: LocalDateTime,
    @Schema(description = LAST_UPDATED_BY_FIELD_DESCRIPTION)
    var lastUpdatedBy: String,
) {
    /**
     * Render the variable definition, so it's suitable for display to humans.
     *
     * @param language The language to render in.
     * @param klassService The service from which to obtain details for classification codes.
     * @return The rendered object.
     */
    fun render(
        language: SupportedLanguages,
        klassService: KlassService,
    ): RenderedView =
        RenderedView(
            id = id,
            patchId = patchId,
            name = name.getValue(language),
            shortName = shortName,
            definition = definition.getValue(language),
            classificationUri = classificationReference?.let { klassService.getKlassUrlForIdAndLanguage(it, language) },
            unitTypes = unitTypes.map { klassService.renderCode(UNIT_TYPES_KLASS_CODE, it, language) },
            subjectFields = subjectFields.map { klassService.renderCode(SUBJECT_FIELDS_KLASS_CODE, it, language) },
            containsSpecialCategoriesOfPersonalData = containsSpecialCategoriesOfPersonalData,
            variableStatus = variableStatus,
            measurementType = measurementType?.let { klassService.renderCode(MEASUREMENT_TYPE_KLASS_CODE, it, language) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            comment = comment?.getValue(language),
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            owner = owner,
            contact = contact.title.getValue(language)?.let { RenderedContact(it, contact.email) },
            lastUpdatedAt = lastUpdatedAt,
            lastUpdatedBy = lastUpdatedBy,
            createdAt = createdAt,
            createdBy = createdBy,
        )
}

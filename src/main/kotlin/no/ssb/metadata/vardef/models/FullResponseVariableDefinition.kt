package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.vardef.constants.*
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

@Serdeable(naming = SnakeCaseStrategy::class)
data class FullResponseVariableDefinition(
    @Schema(description = ID_FIELD_DESCRIPTION)
    var id: String?,
    @Schema(description = PATCH_ID_FIELD_DESCRIPTION)
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
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    val containsSensitivePersonalInformation: Boolean,
    @Schema(
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
    )
    var variableStatus: VariableStatus?,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    val validFrom: LocalDate,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    val validUntil: LocalDate?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    val externalReferenceUri: URL?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    val relatedVariableDefinitionUris: List<URL>?,
    @Schema(description = CONTACT_FIELD_DESCRIPTION)
    val contact: Contact?,
    @Schema(description = CREATED_AT_FIELD_DESCRIPTION)
    var createdAt: LocalDateTime,
    @Schema(description = CREATED_BY_FIELD_DESCRIPTION)
    var createdBy: Person?,
    @Schema(description = LAST_UPDATED_AT_FIELD_DESCRIPTION)
    var lastUpdatedAt: LocalDateTime,
    @Schema(description = LAST_UPDATED_BY_FIELD_DESCRIPTION)
    var lastUpdatedBy: Person?,
)

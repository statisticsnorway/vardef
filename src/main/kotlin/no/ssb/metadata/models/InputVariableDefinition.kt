package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import no.ssb.metadata.constants.*
import no.ssb.metadata.validators.ValidBoolean
import no.ssb.metadata.validators.ValidDate
import no.ssb.metadata.validators.ValidUrl

@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = INPUT_VARIABLE_DEFINITION_EXAMPLE,
)
data class InputVariableDefinition(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    val id: String?,
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[a-z0-9_]{3,}$")
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    // TODO Validate against klass data
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @Pattern(regexp = "^[0-9]+$")
    val classificationReference: String,
    // TODO Validate against klass data
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    @NotEmpty
    val unitTypes: List<String>,
    // TODO Validate against klass data
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @NotEmpty
    val subjectFields: List<String>,
    @Schema(description = CONTAINS_UNIT_IDENTIFYING_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    @ValidBoolean(message = "Invalid value for contains_unit_identifying_information, must be either true or false")
    val containsUnitIdentifyingInformation: String,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    @ValidBoolean(message = "Invalid value for contains_sensitive_personal_information, must be either true or false")
    val containsSensitivePersonalInformation: String,
    @Schema(description = VARIABLE_STATUS_FIELD_DESCRIPTION)
    val variableStatus: VariableStatus,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @ValidDate
    val validFrom: String,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @ValidDate
    val validUntil: String?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    @ValidUrl(message = "Website URL must be valid")
    val externalReferenceUri: String?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<@ValidUrl String>?,
    @Schema(description = CONTACT_FIELD_DESCRIPTION)
    @Valid
    val contact: Contact,
) {
    fun toSavedVariableDefinition(): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = NanoId.generate(8),
            name = name,
            shortName = shortName,
            definition = definition,
            // TODO
            classificationUri = "",
            // TODO
            unitTypes = emptyList(),
            // TODO
            subjectFields = emptyList(),
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation.toBoolean(),
            containsSensitivePersonalInformation = containsSensitivePersonalInformation.toBoolean(),
            variableStatus = variableStatus,
            measurementType = measurementType?.let { KlassReference("", "", it) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris,
            // TODO
            owner = null,
            contact = contact,
            // TODO
            createdAt = "",
            // TODO
            createdBy = null,
            // TODO
            lastUpdatedAt = "",
            // TODO
            lastUpdatedBy = null,
        )
}

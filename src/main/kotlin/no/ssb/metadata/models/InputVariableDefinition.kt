package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.net.URL
import no.ssb.metadata.constants.*
import java.time.LocalDate

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
    @Pattern(regexp = VARDEF_SHORT_NAME_PATTERN)
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    // TODO Validate against klass data
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @Pattern(regexp = KLASS_ID_PATTERN)
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    @NotEmpty
    val unitTypes: List<String>,
    // TODO Validate against klass data
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @NotEmpty
    val subjectFields: List<String>,
    @Schema(description = CONTAINS_UNIT_IDENTIFYING_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    val containsUnitIdentifyingInformation: Boolean,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    val containsSensitivePersonalInformation: Boolean,
    @Schema(description = VARIABLE_STATUS_FIELD_DESCRIPTION)
    val variableStatus: VariableStatus,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Format("yyyy-MM-dd")
    val validFrom: LocalDate,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @Format("yyyy-MM-dd")
    val validUntil: LocalDate?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    val externalReferenceUri: URL?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<URL>?,
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
            containsUnitIdentifyingInformation = containsUnitIdentifyingInformation,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus,//VariableStatus.valueOf(variableStatus),
            measurementType = measurementType?.let { KlassReference("https://example.com/", "", it) },
            validFrom = validFrom,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map{it.toString()},
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

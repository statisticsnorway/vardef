package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.*

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
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @Pattern(regexp = KLASS_ID_PATTERN)
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<String>,
    val subjectFields: List<String>,
    val containsUnitIdentifyingInformation: Boolean,
    val containsSensitivePersonalInformation: Boolean,
    val variableStatus: String,
    @Nullable
    val measurementType: String?,
    @Pattern(regexp = DATE_PATTERN)
    val validFrom: String,
    @Nullable
    @Pattern(regexp = DATE_PATTERN)
    val validUntil: String?,
    @Nullable
    @Pattern(regexp = URL_PATTERN)
    val externalReferenceUri: String?,
    @Nullable
    val relatedVariableDefinitionUris: List<String>?,
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

package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.KlassId
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = INPUT_PATCH_VARIABLE_DEFINITION_EXAMPLE,
)
data class InputPatchVariableDefinition(
    @Nullable
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Nullable
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Nullable
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<
            @KlassCode("702")
            String,
            >,
    @Nullable
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    val subjectFields: List<
            @KlassCode("618")
            String,
            >,
    @Nullable
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    val containsSensitivePersonalInformation: Boolean,
    @Nullable
    @Schema(
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
        accessMode = Schema.AccessMode.READ_ONLY,
    )
    var variableStatus: VariableStatus?,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
    val measurementType: String?,
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
    @Nullable
    val contact: Contact?,
) {
    fun toSavedVariableDefinition(
        previousPatch: SavedVariableDefinition,
        previousPatchId: Int?,
    ): SavedVariableDefinition =
        previousPatch.copy(
            patchId = (previousPatchId ?: 0) + 1,
            name = name,
            definition = definition,
            classificationUri = classificationReference?: previousPatch.classificationUri,
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation ?: previousPatch.containsSensitivePersonalInformation,
            variableStatus = variableStatus ?: previousPatch.variableStatus,
            measurementType = measurementType,
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            // TODO depends on authentication to make user information available
            owner = null,
            contact = contact,
            createdAt = LocalDateTime.now(),
            // TODO depends on authentication to make user information available
            createdBy = null,
            // Provide a placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            // TODO depends on authentication to make user information available
            lastUpdatedBy = null,
        )
}

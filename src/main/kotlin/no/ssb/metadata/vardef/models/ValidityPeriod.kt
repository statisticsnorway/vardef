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

/**
 * Create a new Validity Period on a Published Variable Definition.
 */
@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = VALIDITY_PERIOD_EXAMPLE,
)
data class ValidityPeriod(
    @Schema(description = NAME_FIELD_DESCRIPTION)
    @Nullable
    val name: LanguageStringType?,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    @NotNull
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    @Nullable
    val unitTypes: List<@KlassCode(id = "702") String>?,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @Nullable
    val subjectFields: List<@KlassCode(id = "618") String>?,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @Nullable
    val containsSensitivePersonalInformation: Boolean?,
    @Schema(
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
        accessMode = Schema.AccessMode.READ_ONLY,
    )
    @Nullable
    val variableStatus: VariableStatus?,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
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
    ): SavedVariableDefinition =
        previousPatch.copy(
            patchId = highestPatchId + 1,
            name = name ?: previousPatch.name,
            definition = definition,
            classificationReference = classificationReference ?: previousPatch.classificationReference,
            unitTypes = unitTypes ?: previousPatch.unitTypes,
            subjectFields = subjectFields ?: previousPatch.subjectFields,
            containsSensitivePersonalInformation =
            containsSensitivePersonalInformation ?: previousPatch.containsSensitivePersonalInformation,
            variableStatus = variableStatus ?: previousPatch.variableStatus,
            measurementType = measurementType ?: previousPatch.measurementType,
            validFrom = validFrom,
            externalReferenceUri = externalReferenceUri ?: previousPatch.externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            contact = contact ?: previousPatch.contact,
            // Provide a placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            // TODO depends on authentication to make user information available
            lastUpdatedBy = null,
        )
}

package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.KlassId
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
    example = DRAFT_EXAMPLE,
)
data class Draft(
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    @Nullable
    var id: String?,
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = VARDEF_SHORT_NAME_PATTERN)
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<@KlassCode(id = "702") String>,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    val subjectFields: List<@KlassCode(id = "618") String>,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    val containsSensitivePersonalInformation: Boolean,
    @Schema(
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
        accessMode = Schema.AccessMode.READ_ONLY,
    )
    var variableStatus: VariableStatus?,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Format(DATE_FORMAT)
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
    val contact: Contact?,
) {
    /**
     *
     */
    private fun setOwnerTeam(ownerGroup: String): String {
        return if (ownerGroup.endsWith("data-admins")) {
            ownerGroup.substringBeforeLast("-").substringBeforeLast("-")
        } else {
            ownerGroup.substringBeforeLast("-")
        }
    }

    fun toSavedVariableDefinition(ownerGroup: String): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = id ?: VariableDefinitionService.generateId(),
            patchId = 1,
            name = name,
            shortName = shortName,
            definition = definition,
            classificationUri = classificationReference,
            unitTypes = unitTypes,
            subjectFields = subjectFields,
            containsSensitivePersonalInformation = containsSensitivePersonalInformation,
            variableStatus = variableStatus ?: VariableStatus.DRAFT,
            measurementType = measurementType,
            validFrom = validFrom,
            validUntil = null,
            externalReferenceUri = externalReferenceUri,
            comment = comment,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            owner = Owner(setOwnerTeam(ownerGroup), listOf(ownerGroup)),
            contact = contact,
            // Provide a placeholder value, actual value set by data layer
            createdAt = LocalDateTime.now(),
            // TODO depends on authentication to make user information available
            createdBy = null,
            // Provide a placeholder value, actual value set by data layer
            lastUpdatedAt = LocalDateTime.now(),
            // TODO depends on authentication to make user information available
            lastUpdatedBy = null,
        )
}

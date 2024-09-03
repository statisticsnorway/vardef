package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import no.ssb.metadata.vardef.integrations.klass.validators.KlassId
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = INPUT_VARIABLE_DEFINITION_EXAMPLE,
)
data class InputVariableDefinition(
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
    // TODO Validate against klass data
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<
            @KlassCode("702")
            String,
            >,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    val subjectFields: List<
            @KlassCode("618")
            String,
            >,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @NotNull
    val containsSensitivePersonalInformation: Boolean,
    @Schema(
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
        accessMode = Schema.AccessMode.READ_ONLY,
    )
    var variableStatus: VariableStatus?,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
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
    val contact: Contact?,
) {
    fun toSavedVariableDefinition(previousPatchId: Int?): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = id ?: NanoId.generate(8),
            patchId = (previousPatchId ?: 0) + 1,
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
            validUntil = validUntil,
            externalReferenceUri = externalReferenceUri,
            relatedVariableDefinitionUris = relatedVariableDefinitionUris?.map { it.toString() },
            // TODO depends on authentication to make user information available
            owner = null,
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

    // TODO(move method when new validityPeriod class)
    /**
     *
     */
    fun getDefinitionValue(lang: String): String? {
        return when (lang) {
            "nb" -> definition.nb
            "nn" -> definition.nn
            "en" -> definition.en
            else -> {
                null
            }
        }
    }
}

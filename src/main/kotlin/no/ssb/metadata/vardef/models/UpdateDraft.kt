package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.klass.validators.KlassCode
import java.net.URL
import java.time.LocalDate

/**
 * Update variable definition
 *
 * Data structure with all fields optional for updating an existing variable definition.
 */
@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = DRAFT_EXAMPLE,
)
data class UpdateDraft(
    @Nullable
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType?,
    @Nullable
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = VARDEF_SHORT_NAME_PATTERN)
    val shortName: String?,
    @Nullable
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType?,
    @Nullable
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Pattern(regexp = KLASS_ID_PATTERN)
    val classificationReference: String?,
    @Nullable
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<
            @KlassCode("702")
            String,
            >?,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @Nullable
    val subjectFields: List<
            @KlassCode("618")
            String,
            >?,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @Nullable
    val containsSensitivePersonalInformation: Boolean?,
    @Schema(description = VARIABLE_STATUS_FIELD_DESCRIPTION)
    @Nullable
    val variableStatus: VariableStatus?,
    @Schema(description = MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
    val measurementType: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Nullable
    @Format("yyyy-MM-dd")
    val validFrom: LocalDate?,
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
    @Nullable
    @Valid
    val contact: Contact?,
)

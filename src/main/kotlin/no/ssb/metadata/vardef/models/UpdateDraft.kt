package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.vardef.annotations.KlassCode
import no.ssb.metadata.vardef.annotations.KlassId
import no.ssb.metadata.vardef.constants.*
import java.net.URL
import java.time.LocalDate

/**
 * Update variable definition
 *
 * Data structure with all fields optional for updating a Draft Variable Definition.
 */
@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = DRAFT_EXAMPLE,
)
data class UpdateDraft(
    @Nullable
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType? = null,
    @Nullable
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = VARDEF_SHORT_NAME_PATTERN)
    val shortName: String? = null,
    @Nullable
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType? = null,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String? = null,
    @Nullable
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<@KlassCode(id = "702") String>? = null,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @Nullable
    val subjectFields: List<@KlassCode(id = "618") String>? = null,
    @Schema(description = CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @Nullable
    val containsSensitivePersonalInformation: Boolean? = null,
    @Schema(description = VARIABLE_STATUS_FIELD_DESCRIPTION)
    @Nullable
    val variableStatus: VariableStatus? = null,
    @Schema(description = MEASUREMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
    val measurementType: String? = null,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Nullable
    @Format(DATE_FORMAT)
    val validFrom: LocalDate? = null,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    val externalReferenceUri: URL? = null,
    @Schema(description = COMMENT_FIELD_DESCRIPTION)
    @Nullable
    val comment: LanguageStringType? = null,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<URL>? = null,
    @Schema(description = OWNER_DESCRIPTION)
    @Nullable
    @Valid
    val owner: Owner? = null,
    @Schema(description = CONTACT_FIELD_DESCRIPTION)
    @Nullable
    @Valid
    val contact: Contact? = null,
)

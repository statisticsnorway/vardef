package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
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
    example = no.ssb.metadata.vardef.constants.INPUT_VARIABLE_DEFINITION_EXAMPLE,
)
data class UpdateVariableDefinition(
    @Nullable
    @Schema(description = no.ssb.metadata.vardef.constants.NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType?,
    @Nullable
    @Schema(description = no.ssb.metadata.vardef.constants.SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = no.ssb.metadata.vardef.constants.VARDEF_SHORT_NAME_PATTERN)
    val shortName: String?,
    @Nullable
    @Schema(description = no.ssb.metadata.vardef.constants.DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType?,
    @Nullable
    @Schema(description = no.ssb.metadata.vardef.constants.CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Pattern(regexp = no.ssb.metadata.vardef.constants.KLASS_ID_PATTERN)
    val classificationReference: String?,
    @Nullable
    @Schema(description = no.ssb.metadata.vardef.constants.UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<
            @KlassCode("702")
            String,
            >?,
    @Schema(description = no.ssb.metadata.vardef.constants.SUBJECT_FIELDS_FIELD_DESCRIPTION)
    @Nullable
    val subjectFields: List<
            @KlassCode("618")
            String,
            >?,
    @Schema(description = no.ssb.metadata.vardef.constants.CONTAINS_SENSITIVE_PERSONAL_INFORMATION_FIELD_DESCRIPTION)
    @Nullable
    val containsSensitivePersonalInformation: Boolean?,
    @Schema(description = no.ssb.metadata.vardef.constants.VARIABLE_STATUS_FIELD_DESCRIPTION)
    @Nullable
    val variableStatus: VariableStatus?,
    @Schema(description = no.ssb.metadata.vardef.constants.MEASURMENT_TYPE_FIELD_DESCRIPTION)
    @Nullable
    @KlassCode("303")
    val measurementType: String?,
    @Schema(description = no.ssb.metadata.vardef.constants.VALID_FROM_FIELD_DESCRIPTION)
    @Nullable
    @Format("yyyy-MM-dd")
    val validFrom: LocalDate?,
    @Schema(description = no.ssb.metadata.vardef.constants.VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @Format("yyyy-MM-dd")
    val validUntil: LocalDate?,
    @Schema(description = no.ssb.metadata.vardef.constants.EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    @Nullable
    val externalReferenceUri: URL?,
    @Schema(description = no.ssb.metadata.vardef.constants.RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    @Nullable
    val relatedVariableDefinitionUris: List<URL>?,
    @Schema(description = no.ssb.metadata.vardef.constants.CONTACT_FIELD_DESCRIPTION)
    @Nullable
    @Valid
    val contact: Contact?,
)

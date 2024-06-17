package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.*

/**
 * Update variable definition
 *
 * Data structure with all fields optional for updating an existing variable definition.
 */
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = UPDATE_VARIABLE_DEFINITION,
)
data class UpdateVariableDefinition(
    @Nullable
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType?,
    @Nullable
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[a-z0-9_]{3,}$")
    val shortName: String?,
    @Nullable
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType?,
    @Nullable
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[0-9]+$")
    val classificationReference: String?,
    @Nullable
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<String>?,
    @Nullable
    val subjectFields: List<String>?,
    @Nullable
    val containsUnitIdentifyingInformation: Boolean?,
    @Nullable
    val containsSensitivePersonalInformation: Boolean?,
    @Nullable
    val variableStatus: String?,
    @Nullable
    val measurementType: String?,
    @Nullable
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
    val validFrom: String?,
    @Nullable
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$")
    val validUntil: String?,
    @Nullable
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].\\S*$")
    val externalReferenceUri: String?,
    @Nullable
    val relatedVariableDefinitionUris: List<String>?,
    @Nullable
    val contact: Contact?,
)

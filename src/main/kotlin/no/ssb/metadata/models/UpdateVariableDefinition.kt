package no.ssb.metadata.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.*
import java.net.URL
import java.time.LocalDate

/**
 * Update variable definition
 *
 * Data structure with all fields optional for updating an existing variable definition.
 */
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = INPUT_VARIABLE_DEFINITION_EXAMPLE,
)
data class UpdateVariableDefinition(
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
    val unitTypes: List<String>?,
    @Nullable
    val subjectFields: List<String>?,
    @Nullable
    val containsUnitIdentifyingInformation: Boolean?,
    @Nullable
    val containsSensitivePersonalInformation: Boolean?,
    @Nullable
    val variableStatus: VariableStatus?,
    @Nullable
    val measurementType: String?,
    @Nullable
    @Pattern(regexp = DATE_PATTERN)
    val validFrom: LocalDate?,
    @Nullable
    @Pattern(regexp = DATE_PATTERN)
    val validUntil: LocalDate?,
    @Nullable
    @Pattern(regexp = URL_PATTERN)
    val externalReferenceUri: URL?,
    @Nullable
    val relatedVariableDefinitionUris: List<URL>?,
    @Nullable
    val contact: Contact?,
)

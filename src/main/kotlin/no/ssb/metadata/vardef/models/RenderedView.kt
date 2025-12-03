package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.vardef.constants.CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.COMMENT_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.CONTACT_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.CONTAINS_SPECIAL_CATEGORIES_OF_PERSONAL_DATA_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.CREATED_AT_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.CREATED_BY_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.DEFINITION_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.ID_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.LAST_UPDATED_AT_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.LAST_UPDATED_BY_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.MEASUREMENT_TYPE_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.NAME_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.OWNER_DESCRIPTION
import no.ssb.metadata.vardef.constants.PATCH_ID_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.RENDERED_VIEW_EXAMPLE
import no.ssb.metadata.vardef.constants.SHORT_NAME_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.SUBJECT_FIELDS_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.UNIT_TYPES_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.VALID_FROM_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.VALID_UNTIL_FIELD_DESCRIPTION
import no.ssb.metadata.vardef.constants.VARDEF_ID_PATTERN
import no.ssb.metadata.vardef.constants.VARIABLE_STATUS_FIELD_DESCRIPTION
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Render a Variable Definition in a specific language,
 * for display to end users.
 */
@Schema(
    example = RENDERED_VIEW_EXAMPLE,
)
@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedView(
    @Schema(description = ID_FIELD_DESCRIPTION, format = VARDEF_ID_PATTERN)
    val id: String,
    @Schema(description = PATCH_ID_FIELD_DESCRIPTION, example = "1")
    val patchId: Int,
    @Schema(
        description = NAME_FIELD_DESCRIPTION,
    )
    val name: String?, // Nullable due to language selection
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: String?, // Nullable due to language selection
    @Schema(description = "Link to the classification which defines all permitted values for this variable.")
    val classificationUri: String?,
    @Schema(description = UNIT_TYPES_FIELD_DESCRIPTION)
    val unitTypes: List<KlassReference>,
    @Schema(description = SUBJECT_FIELDS_FIELD_DESCRIPTION)
    val subjectFields: List<KlassReference>,
    @Schema(description = CONTAINS_SPECIAL_CATEGORIES_OF_PERSONAL_DATA_FIELD_DESCRIPTION)
    val containsSpecialCategoriesOfPersonalData: Boolean,
    @Schema(
        title = "VariableStatus",
        description = VARIABLE_STATUS_FIELD_DESCRIPTION,
        implementation = VariableStatus::class,
    )
    var variableStatus: VariableStatus,
    @Schema(
        title = "KlassReference",
        description = MEASUREMENT_TYPE_FIELD_DESCRIPTION,
        implementation = KlassReference::class,
    )
    val measurementType: KlassReference?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    val validFrom: LocalDate,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    val validUntil: LocalDate?,
    @Schema(description = EXTERNAL_REFERENCE_URI_FIELD_DESCRIPTION)
    val externalReferenceUri: URL?,
    @Schema(description = COMMENT_FIELD_DESCRIPTION)
    val comment: String?,
    @Schema(description = RELATED_VARIABLE_DEFINITION_URIS_FIELD_DESCRIPTION)
    val relatedVariableDefinitionUris: List<URL>?,
    @Schema(
        title = "Owner",
        description = OWNER_DESCRIPTION,
        implementation = Owner::class,
    )
    val owner: Owner,
    @Schema(
        title = "RenderedContact",
        description = CONTACT_FIELD_DESCRIPTION,
        implementation = RenderedContact::class,
    )
    val contact: RenderedContact?,
    @Schema(description = CREATED_AT_FIELD_DESCRIPTION)
    var createdAt: LocalDateTime,
    @Schema(description = CREATED_BY_FIELD_DESCRIPTION)
    var createdBy: String,
    @Schema(description = LAST_UPDATED_AT_FIELD_DESCRIPTION)
    val lastUpdatedAt: LocalDateTime,
    @Schema(description = LAST_UPDATED_BY_FIELD_DESCRIPTION)
    var lastUpdatedBy: String,
)

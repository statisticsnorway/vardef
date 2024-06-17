package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.constants.RENDERED_VARIABLE_DEFINITION_EXAMPLE

@Schema(
    example = RENDERED_VARIABLE_DEFINITION_EXAMPLE,
)
@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedVariableDefinition(
    val id: String,
    val name: String?,
    val shortName: String,
    val definition: String?,
    val classificationUri: String?,
    val unitTypes: List<KlassReference>,
    val subjectFields: List<KlassReference>,
    val containsUnitIdentifyingInformation: Boolean,
    val containsSensitivePersonalInformation: Boolean,
    val variableStatus: String,
    val measurementType: KlassReference?,
    val validFrom: String,
    val validUntil: String?,
    val externalReferenceUri: String?,
    val relatedVariableDefinitionUris: List<String>?,
    val owner: Owner?,
    val contact: RenderedContact,
    val createdAt: String,
    val createdBy: Person?,
    val lastUpdatedAt: String,
    val lastUpdatedBy: Person?,
)

package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.vardef.constants.RENDERED_VARIABLE_DEFINITION_EXAMPLE
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

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
    val unitTypes: List<KlassReference?>,
    val subjectFields: List<KlassReference?>,
    val containsSensitivePersonalInformation: Boolean,
    val variableStatus: VariableStatus,
    val measurementType: KlassReference?,
    val validFrom: LocalDate,
    val validUntil: LocalDate?,
    val externalReferenceUri: URL?,
    val relatedVariableDefinitionUris: List<URL>?,
    val owner: Owner?,
    val contact: RenderedContact,
    val createdAt: LocalDateTime,
    val createdBy: Person?,
    val lastUpdatedAt: LocalDateTime,
    val lastUpdatedBy: Person?,
)

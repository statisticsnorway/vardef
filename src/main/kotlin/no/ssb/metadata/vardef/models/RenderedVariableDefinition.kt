package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.vardef.constants.RENDERED_VARIABLE_DEFINITION_EXAMPLE
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Render a Variable Definition in a specific language, for display to end users.
 */
@Schema(
    example = RENDERED_VARIABLE_DEFINITION_EXAMPLE,
)
@Serdeable(naming = SnakeCaseStrategy::class)
data class RenderedVariableDefinition(
    val id: String,
    val patchId: Int,
    val name: String?,
    val shortName: String,
    val definition: String?,
    val classificationUri: String?,
    val unitTypes: List<KlassReference?>,
    val subjectFields: List<KlassReference?>,
    val containsSensitivePersonalInformation: Boolean,
    val measurementType: KlassReference?,
    val validFrom: LocalDate,
    val validUntil: LocalDate?,
    val externalReferenceUri: URL?,
    val relatedVariableDefinitionUris: List<URL>?,
    val contact: RenderedContact?,
    val lastUpdatedAt: LocalDateTime,
)

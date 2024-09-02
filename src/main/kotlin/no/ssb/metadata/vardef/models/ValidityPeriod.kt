package no.ssb.metadata.vardef.models

import io.micronaut.core.annotation.Nullable
import io.micronaut.core.convert.format.Format
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import no.ssb.metadata.vardef.constants.*
import no.ssb.metadata.vardef.integrations.klass.validators.KlassId
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("ktlint:standard:annotation", "ktlint:standard:indent") // ktlint disagrees with the formatter
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = VALIDITY_PERIOD_EXAMPLE,
)
class ValidityPeriod(
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
    @Schema(description = CLASSIFICATION_REFERENCE_FIELD_DESCRIPTION)
    @Nullable
    @KlassId
    val classificationReference: String?,
    @Schema(description = VALID_FROM_FIELD_DESCRIPTION)
    @Format("yyyy-MM-dd")
    val validFrom: LocalDate,
    @Schema(description = VALID_UNTIL_FIELD_DESCRIPTION)
    @Nullable
    @Format("yyyy-MM-dd")
    val validUntil: LocalDate?,
    ) {
        fun toSavedVariableDefinition(
            previousPatchId: Int?,
            savedVariableDefinition: SavedVariableDefinition,
        ): SavedVariableDefinition =
        SavedVariableDefinition(
            definitionId = savedVariableDefinition.definitionId,
            patchId = (previousPatchId ?: 0) + 1,
            name = name,
            shortName = savedVariableDefinition.shortName,
            definition = definition,
            classificationUri = classificationReference,
            validFrom = validFrom,
            validUntil = validUntil,
            unitTypes = savedVariableDefinition.unitTypes,
            subjectFields = savedVariableDefinition.subjectFields,
            containsSensitivePersonalInformation = savedVariableDefinition.containsSensitivePersonalInformation,
            variableStatus = savedVariableDefinition.variableStatus,
            measurementType = savedVariableDefinition.measurementType,
            externalReferenceUri = savedVariableDefinition.externalReferenceUri,
            relatedVariableDefinitionUris = savedVariableDefinition.relatedVariableDefinitionUris,
            owner = savedVariableDefinition.owner,
            contact = savedVariableDefinition.contact,
            createdAt = LocalDateTime.now(),
            createdBy = null,
            lastUpdatedAt = LocalDateTime.now(),
            lastUpdatedBy = null,
        )

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

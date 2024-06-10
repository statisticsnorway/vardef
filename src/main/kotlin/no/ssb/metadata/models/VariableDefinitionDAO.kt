package no.ssb.metadata.models

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import no.ssb.metadata.constants.DEFINITION_FIELD_DESCRIPTION
import no.ssb.metadata.constants.NAME_FIELD_DESCRIPTION
import no.ssb.metadata.constants.SHORT_NAME_FIELD_DESCRIPTION
import org.bson.types.ObjectId

@MappedEntity(namingStrategy = NamingStrategies.Raw::class)
data class VariableDefinitionDAO(
    var definitionId: String,
    @field:Id @GeneratedValue
    var id: ObjectId? = null,
    @Schema(description = NAME_FIELD_DESCRIPTION)
    val name: LanguageStringType,
    @Schema(description = SHORT_NAME_FIELD_DESCRIPTION)
    @Pattern(regexp = "^[a-z0-9_]{3,}$")
    val shortName: String,
    @Schema(description = DEFINITION_FIELD_DESCRIPTION)
    val definition: LanguageStringType,
) {
    fun toDTO(language: SupportedLanguages): VariableDefinitionDTO =
        VariableDefinitionDTO(
            id = definitionId,
            name = name.getValidLanguage(language),
            shortName = shortName,
            definition = definition.getValidLanguage(language),
        )

    fun toInput(): InputVariableDefinition =
        InputVariableDefinition(
            id = definitionId,
            name = name,
            shortName = shortName,
            definition = definition,
        )
}

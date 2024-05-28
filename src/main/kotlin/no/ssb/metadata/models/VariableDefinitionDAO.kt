package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import org.bson.types.ObjectId

@MappedEntity
@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = """
        {
            "name":
                {   "en": "English",
                    "nb": "Norwegian Bokmål",
                    "nn": "Norwegian Nynorsk"
                },
            "short_name": "string",
            "definition":
                {
                    "en": "English",
                    "nb": "Norwegian Bokmål",
                    "nn": "Norwegian Nynorsk"
                }
        }
    """,
)
data class VariableDefinitionDAO(
    @field:Id @GeneratedValue @JsonIgnore val id: ObjectId?,
    @field:NotEmpty val name: LanguageStringType,
    @field:NotEmpty val shortName: String,
    @field:NotEmpty val definition: LanguageStringType
) {
    fun toDTO(language: String): VariableDefinitionDTO =
        VariableDefinitionDTO(
            name = name.getValidLanguage(language),
            shortName = shortName,
            definition = definition.getValidLanguage(language),
        )
}

package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import io.viascom.nanoid.NanoId
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
    @field:NotEmpty val name: Map<SupportedLanguages, String>,
    @field:NotEmpty val shortName: String,
    @field:NotEmpty val definition: Map<SupportedLanguages, String>,
    @field:NotEmpty val varDefId: String = NanoId.generate(8),
) {
    fun getName(language: String): String? =
        this.name
            .map { it.key to it.value }
            .firstOrNull { it.first.toString() == language }
            ?.second

    fun getDefinition(language: String): String? =
        this.definition
            .map { it.key to it.value }
            .firstOrNull { it.first.toString() == language }
            ?.second

    fun toDTO(language: String): VariableDefinitionDTO =
        VariableDefinitionDTO(
            id = varDefId,
            name = this.getName(language),
            shortName = shortName,
            definition = this.getDefinition(language),
        )
}

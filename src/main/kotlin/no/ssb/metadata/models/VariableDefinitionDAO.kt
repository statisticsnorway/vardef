package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema
import org.bson.types.ObjectId

@MappedEntity
@Introspected
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
class VariableDefinitionDAO(
    @field:Id
    @GeneratedValue
    @JsonIgnore
    var id: ObjectId?,
    var name: Map<SupportedLanguages, String>,
    var shortName: String,
    var definition: Map<SupportedLanguages, String>,
) {
    fun getName(language: String): String? {
        var nameByLanguage: String = ""
        for ((k, v) in this.name) {
            if (k.toString() == language) {
                nameByLanguage = v
                return nameByLanguage
            }
        }
        return null
    }

    fun getDefinition(language: String): String {
        var definitionByLanguage: String = ""
        for ((k, v) in this.definition) {
            if (k.toString() == language) {
                definitionByLanguage = v
            }

        }
        return definitionByLanguage
    }
}

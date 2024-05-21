package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema
import org.bson.types.ObjectId

@MappedEntity
@Serdeable
// @Suppress("ktlint:standard:max-line-length")
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
    var shortName: String?,
    var definition: Map<SupportedLanguages, String>,
) {

    fun getName(language: String?): Map<SupportedLanguages, String> {
        val inputLanguage = language ?: "nb"
        var nameByLanguage = mapOf<SupportedLanguages,String>()
        for ((k, v) in this.name) {
            if (k.toString() == inputLanguage) {
                nameByLanguage = mapOf(k to v)
            }
        }
        return nameByLanguage
    }

    fun getDefinition(language: String?): Map<SupportedLanguages, String> {
        val inputLanguage = language ?: "nb"
        var definitionByLanguage = mapOf<SupportedLanguages,String>()
        for ((k, v) in this.definition) {
            if (k.toString() == inputLanguage) {
                definitionByLanguage = mapOf(k to v)
            }
        }
        return definitionByLanguage
    }
}

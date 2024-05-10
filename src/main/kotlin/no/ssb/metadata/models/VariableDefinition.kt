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
@Schema(
    example = "{\"name\": {\"en\": \"English\",\"nb\": \"Norwegian Bokmål\",\"nn\": \"Norwegian Nynorsk\"},\"short_name\": \"string\",\"definition\": {\"en\": \"English\",\"nb\": \"Norwegian Bokmål\",\"nn\": \"Norwegian Nynorsk\"}}",
)
data class VariableDefinition(
    @field:Id
    @GeneratedValue
    @JsonIgnore
    var id: ObjectId?,
    var name: Map<SupportedLanguages, String>,
    var shortName: String?,
    var definition: Map<SupportedLanguages, String>,
)

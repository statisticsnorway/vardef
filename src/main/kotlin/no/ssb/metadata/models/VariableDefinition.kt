package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import org.bson.types.ObjectId


@MappedEntity
@Serdeable(naming=SnakeCaseStrategy::class)
data class VariableDefinition(
    @field:Id
    @GeneratedValue
    @JsonIgnore
    var id: ObjectId?,
    var name: Map<SupportedLanguages, String>,
    var shortName: String?,
    var definition: Map<SupportedLanguages, String>
)



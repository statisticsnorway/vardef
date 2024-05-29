package no.ssb.metadata.models

import com.fasterxml.jackson.annotation.JsonIgnore
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = """
        {
            "name": "value",
            "short_name": "string",
            "definition": "value"
        }
    """,
)
data class VariableDefinitionDTO(
    @JsonIgnore val id: String?,
    val name: String?,
    val shortName: String,
    val definition: String?,
)

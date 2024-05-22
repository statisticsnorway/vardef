package no.ssb.metadata.models

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
    var name: String?,
    var shortName: String,
    var definition: String?,
)

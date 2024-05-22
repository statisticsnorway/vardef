package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable
import io.micronaut.serde.config.naming.SnakeCaseStrategy
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable(naming = SnakeCaseStrategy::class)
@Schema(
    example = """
        {
            "name": {"language code": "value"},
            "short_name": "string",
            "definition": {"language code": "value"}
        }
    """,
)
data class VariableDefinitionDTO(
    var name: Map<SupportedLanguages, String>,
    var shortName: String,
    var definition: Map<SupportedLanguages, String>,
)

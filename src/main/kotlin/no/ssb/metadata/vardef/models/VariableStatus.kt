package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable
import io.swagger.v3.oas.annotations.media.Schema

@Serdeable
@Schema(description = "Life cycle status of a variable definition")
enum class VariableStatus {
    DRAFT,
    PUBLISHED_INTERNAL,
    PUBLISHED_EXTERNAL,
    DEPRECATED,
}

fun VariableStatus.isPublished(): Boolean =
    this in
        listOf(VariableStatus.PUBLISHED_EXTERNAL, VariableStatus.PUBLISHED_INTERNAL)

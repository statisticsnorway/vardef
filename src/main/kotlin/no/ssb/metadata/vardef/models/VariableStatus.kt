package no.ssb.metadata.vardef.models

import io.micronaut.serde.annotation.Serdeable

/**
 * Life cycle status of a variable definition.
 */
@Serdeable
enum class VariableStatus {
    DRAFT,
    PUBLISHED_INTERNAL,
    PUBLISHED_EXTERNAL,
}

fun VariableStatus.isPublished(): Boolean = this in listOf(VariableStatus.PUBLISHED_EXTERNAL, VariableStatus.PUBLISHED_INTERNAL)

fun VariableStatus.isPublic(): Boolean = this == VariableStatus.PUBLISHED_EXTERNAL

fun VariableStatus.canTransitionTo(targetStatus: VariableStatus): Boolean =
    when (targetStatus) {
        VariableStatus.DRAFT -> this == VariableStatus.DRAFT
        VariableStatus.PUBLISHED_INTERNAL -> this == VariableStatus.DRAFT
        VariableStatus.PUBLISHED_EXTERNAL -> this == VariableStatus.DRAFT || this == VariableStatus.PUBLISHED_INTERNAL
    }

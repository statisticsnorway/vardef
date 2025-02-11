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

/**
 * Determines if a variable status can transition to a target status.
 *
 * @receiver The current status of the variable.
 * @param targetStatus The status to which the transition is being checked.
 * @return `true` if the transition from the current status to the target status is allowed, otherwise `false`.
 *
 * Transition Rules:
 * - `DRAFT` can only transition to `DRAFT`.
 * - `PUBLISHED_INTERNAL` can transition from `DRAFT` or `PUBLISHED_INTERNAL`.
 * - `PUBLISHED_EXTERNAL` can transition from `DRAFT`, `PUBLISHED_INTERNAL`, or `PUBLISHED_EXTERNAL`.
 */
fun VariableStatus.canTransitionTo(targetStatus: VariableStatus): Boolean =
    when (targetStatus) {
        VariableStatus.DRAFT -> this == VariableStatus.DRAFT
        VariableStatus.PUBLISHED_INTERNAL -> this == VariableStatus.DRAFT || this == VariableStatus.PUBLISHED_INTERNAL
        VariableStatus.PUBLISHED_EXTERNAL ->
            this == VariableStatus.DRAFT ||
                this == VariableStatus.PUBLISHED_INTERNAL ||
                this == VariableStatus.PUBLISHED_EXTERNAL
    }

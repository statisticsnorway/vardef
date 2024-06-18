package no.ssb.metadata.validators

import no.ssb.metadata.models.VariableStatus


object ValidVariableStatusValidator {
    fun isValid(value: String): Boolean {
        return VariableStatus.entries.any { it.name == value }
    }
}

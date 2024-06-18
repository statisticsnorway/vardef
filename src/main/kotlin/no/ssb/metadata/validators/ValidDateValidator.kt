package no.ssb.metadata.validators

import no.ssb.metadata.constants.DATE_PATTERN

object ValidDateValidator {
    fun isValid(value: String?): Boolean {
        val regex = DATE_PATTERN.toRegex()

        return value.isNullOrBlank() || regex.matches(value)
    }
}

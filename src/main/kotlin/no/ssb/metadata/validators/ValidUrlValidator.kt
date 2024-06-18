package no.ssb.metadata.validators

import no.ssb.metadata.constants.URL_PATTERN

object ValidUrlValidator {
    fun isValid(value: String?): Boolean {
        val regex = URL_PATTERN.toRegex()

        return value.isNullOrBlank() || regex.matches(value)
    }
}

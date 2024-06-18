package no.ssb.metadata.validators


object ValidDateValidator {
    fun isValid(value: String?): Boolean {

        val regex = "^\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$".toRegex()

        return value.isNullOrBlank() || regex.matches(value)
    }
}
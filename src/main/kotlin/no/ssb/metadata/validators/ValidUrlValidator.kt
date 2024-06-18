package no.ssb.metadata.validators


object ValidUrlValidator {
    fun isValid(value: String?): Boolean {

        val regex = "^(https?|ftp)://[^\\s/$.?#].\\S*$".toRegex()

        return value.isNullOrBlank() || regex.matches(value)
    }
}
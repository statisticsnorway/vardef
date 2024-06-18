package no.ssb.metadata.validators


object ValidBooleanValidator {
    fun isValid(value: String?): Boolean {
        return value != null && (value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true))
    }
}
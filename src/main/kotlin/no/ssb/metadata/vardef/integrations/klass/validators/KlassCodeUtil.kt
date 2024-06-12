package no.ssb.metadata.vardef.integrations.klass.validators

object KlassCodeUtil {
    private val klassCodes = intArrayOf(131, 17, 68)

    fun isValid(value: Int?): Boolean {
        if (value == null) {
            return false
        }
        if (value in klassCodes) {
            return true
        }
        return false
    }
}

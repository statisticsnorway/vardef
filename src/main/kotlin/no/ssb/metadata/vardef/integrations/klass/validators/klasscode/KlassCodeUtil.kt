package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import no.ssb.metadata.vardef.integrations.klass.validators.TestCodes

object KlassCodeUtil {
    fun isValidCode(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        return TestCodes.testDataCodes.map { item -> item.unitCode }.contains(value)
    }
}

package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import com.mongodb.internal.authentication.AwsCredentialHelper.LOGGER
import no.ssb.metadata.vardef.integrations.klass.validators.TestCodes

object KlassCodesUtil {
    fun isValidCodes(values: List<String>): Boolean {
        val unitCodes = TestCodes.testDataCodes.map { it.unitCode }

        val hasValidCodes = values.any { code -> unitCodes.contains(code) }
        val hasInvalidCodes = values.any { code -> !unitCodes.contains(code) }

        LOGGER.info("Valid values: $hasValidCodes, invalid values: $hasInvalidCodes")

        return hasValidCodes && !hasInvalidCodes
    }
}

object KlassCodeUtil {
    fun isValidCode(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        return TestCodes.testDataCodes.map { item -> item.unitCode }.contains(value)
    }
}

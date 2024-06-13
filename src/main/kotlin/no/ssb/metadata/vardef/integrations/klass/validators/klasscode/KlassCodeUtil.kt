package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import com.mongodb.internal.authentication.AwsCredentialHelper.LOGGER
import no.ssb.metadata.vardef.integrations.klass.validators.TestCodes

object KlassCodeUtil {
    fun isValidCode(values: List<String>): Boolean {
        val unitCodes = TestCodes.testDataCodes.map { it.unitCode }

        val hasValidCodes = values.any { code -> unitCodes.contains(code) }
        val hasInvalidCodes = values.any { code -> !unitCodes.contains(code) }

        LOGGER.info("Valid values: $hasValidCodes, invalid values: $hasInvalidCodes")

        return hasValidCodes && !hasInvalidCodes
    }
}

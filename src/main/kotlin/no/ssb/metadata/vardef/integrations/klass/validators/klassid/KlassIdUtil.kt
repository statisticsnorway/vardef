package no.ssb.metadata.vardef.integrations.klass.validators.klassid

import no.ssb.metadata.vardef.integrations.klass.validators.TestCacheObjectClassification

object KlassIdUtil {
    fun isValidId(value: String?): Boolean {
        return TestCacheObjectClassification.classificationItems.map { item -> item.id }.contains(value?.toInt())
    }
}

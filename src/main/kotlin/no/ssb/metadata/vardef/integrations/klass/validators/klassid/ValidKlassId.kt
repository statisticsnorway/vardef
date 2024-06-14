package no.ssb.metadata.vardef.integrations.klass.validators.klassid

import no.ssb.metadata.vardef.integrations.klass.validators.TestCacheObjectClassification

/*
TODO: Edit source when caching object is implemented
 */
object ValidKlassId {
    fun isValidId(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        return TestCacheObjectClassification.classificationItems.map { item -> item.id }.contains(value.toInt())
    }
}

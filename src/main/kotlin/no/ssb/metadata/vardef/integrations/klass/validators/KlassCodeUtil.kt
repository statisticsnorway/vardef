package no.ssb.metadata.vardef.integrations.klass.validators

import no.ssb.metadata.vardef.integrations.klass.models.Classification
import no.ssb.metadata.vardef.integrations.klass.models.Classifications

object KlassCodeUtil {
    private val klassCodes = intArrayOf(131, 17, 68)

   // private val classifications = List<Classification>


    //private val testCacheObject = Classifications(List)

    fun isValid(value: String?): Boolean {
        return value!!.toIntOrNull()!! in klassCodes
    }
}


package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import no.ssb.metadata.vardef.integrations.klass.validators.TestSubjectCodes
import no.ssb.metadata.vardef.integrations.klass.validators.TestUnitCodes

/*
TODO: Edit source when caching object is implemented
 */
object ValidKlassCode {
    fun isValidKlassCode(value: String?, id: String): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        return when (id) {
            "702" -> TestUnitCodes.testDataCodes.map { item -> item.unitCode }.contains(value)
            "618" -> TestSubjectCodes.testDataCodesSubjectField.map { item -> item.subjectCode }.contains(value)
            else -> false
        }
    }
}

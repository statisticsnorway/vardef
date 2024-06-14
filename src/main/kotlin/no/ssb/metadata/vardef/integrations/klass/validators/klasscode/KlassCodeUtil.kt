package no.ssb.metadata.vardef.integrations.klass.validators.klasscode

import no.ssb.metadata.vardef.integrations.klass.validators.TestSubjectCodes
import no.ssb.metadata.vardef.integrations.klass.validators.TestUnitCodes

/*
TODO: Edit source when caching object is implemented
 */
object ValidKlassCodeUnitType {
    fun isValidUnitCode(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        return TestUnitCodes.testDataCodes.map { item -> item.unitCode }.contains(value)
    }
}

/*
TODO: Edit source when caching object is implemented
 */
object ValidKlassCodeSubjectField {
    fun isValidSubjectCode(value: String?): Boolean {
        if (value.isNullOrEmpty()) {
            return false
        }
        return TestSubjectCodes.testDataCodes.map { item -> item.subjectCode }.contains(value)
    }
}

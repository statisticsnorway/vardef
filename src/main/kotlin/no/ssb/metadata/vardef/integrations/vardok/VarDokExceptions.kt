package no.ssb.metadata.vardef.integrations.vardok

open class VardokException(override val message: String) : Exception()

class MissingDataElementNameException : VardokException("Vardok is missing short name and can not be saved")

class MissingValidDatesException : VardokException("Vardok is missing valid dates and can not be saved")

fun checkVardokForMissingElements(varDokItems: MutableMap<String, FIMD?>) {
    if (varDokItems["nb"]?.variable?.dataElementName.isNullOrBlank()) {
        throw MissingDataElementNameException()
    }
    if (varDokItems["nb"]?.dc?.valid.isNullOrBlank()) {
        throw MissingValidDatesException()
    }
}

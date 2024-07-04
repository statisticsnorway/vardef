package no.ssb.metadata.vardef.integrations.vardok

open class VardokException(override val message: String) : Exception()

class MissingDataElementNameException() : VardokException("Variabledefinition from Vardok is missing data element name")

class MissingValidDatesException() : VardokException("Vardok is missing valid dates")

fun vardokMissingElements(varDokItems: MutableMap<String, FIMD>) {
    if (varDokItems["nb"]?.variable?.dataElementName.isNullOrBlank()) {
        throw MissingDataElementNameException()
    }
    if (varDokItems["nb"]?.dc?.valid.isNullOrBlank()) {
        throw MissingValidDatesException()
    }
}

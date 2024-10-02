package no.ssb.metadata.vardef.integrations.vardok

open class VardokException(override val message: String, val id: String?) : Exception()

class MissingDataElementNameException(id: String) :
    VardokException("Vardok id $id is missing data element name (short name) and can not be saved", id = null)

class MissingValidDatesException(id: String) :
    VardokException("Vardok id $id is missing valid dates and can not be saved", id = null)

class VardokNotFoundException(id: String) : VardokException("Vardok id $id not found", id = null)

fun checkVardokForMissingElements(varDokItems: MutableMap<String, VardokResponse>) {
    if (varDokItems["nb"]?.variable?.dataElementName.isNullOrBlank()) {
        throw MissingDataElementNameException(varDokItems["nb"]?.id?.substringAfterLast(":").toString())
    }
    if (varDokItems["nb"]?.dc?.valid.isNullOrBlank()) {
        throw MissingValidDatesException(varDokItems["nb"]?.id?.substringAfterLast(":").toString())
    }
}

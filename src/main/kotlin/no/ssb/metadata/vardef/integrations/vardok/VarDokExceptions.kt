package no.ssb.metadata.vardef.integrations.vardok

open class VardokException(override val message: String, val id: String) : Exception()

class MissingDataElementNameException(id: String) :
    VardokException("Vardok id $id is missing DataElementName (short name) and can not be saved", id)

class MissingValidDatesException(id: String) :
    VardokException("Vardok id $id is missing Valid (valid dates) and can not be saved", id)

class MissingValidFromException(id: String) : VardokException(
    "Vardok id $id Valid is missing 'from' date and can not be saved",
    id,
)

class VardokNotFoundException(id: String) : VardokException("Vardok id $id not found", id)

class IllegalShortNameException(id: String) : VardokException(
    "Vardok id $id DataElementName does not conform to short name rules and can not be saved",
    id,
)

class OutdatedUnitTypesException(id: String) : VardokException(
    "Vardok id $id StatisticalUnit has outdated unit types and can not be saved",
    id,
)

fun checkVardokForMissingElements(varDokItems: MutableMap<String, VardokResponse>) {
    if (varDokItems["nb"]?.variable?.dataElementName.isNullOrBlank()) {
        throw MissingDataElementNameException(varDokItems["nb"]?.id?.substringAfterLast(":").toString())
    }
    if (varDokItems["nb"]?.dc?.valid.isNullOrBlank()) {
        throw MissingValidDatesException(varDokItems["nb"]?.id?.substringAfterLast(":").toString())
    }
}

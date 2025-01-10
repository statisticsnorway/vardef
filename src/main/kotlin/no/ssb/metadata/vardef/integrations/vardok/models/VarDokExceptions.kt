package no.ssb.metadata.vardef.integrations.vardok.models

open class VardokException(
    override val message: String,
) : Exception()

class MissingValidDatesException(
    id: String,
) : VardokException("Vardok id $id is missing Valid (valid dates) and can not be saved")

class MissingValidFromException(
    id: String,
) : VardokException(
        "Vardok id $id Valid is missing 'from' date and can not be saved",
    )

class MissingNbLanguageException :
    VardokException(
        "The VarDok definition is missing the Norwegian Bokmål language and can not be migrated.",
    )

class VardokNotFoundException(
    id: String,
) : VardokException("Vardok id $id not found")

class OutdatedUnitTypesException(
    id: String,
) : VardokException(
        "Vardok id $id StatisticalUnit has outdated unit types and can not be saved",
    )

fun checkVardokForMissingElements(varDokItems: Map<String, VardokResponse>) {
    if (varDokItems["nb"]?.dc?.valid.isNullOrBlank()) {
        throw MissingValidDatesException(varDokItems["nb"]?.id?.substringAfterLast(":").toString())
    }
}

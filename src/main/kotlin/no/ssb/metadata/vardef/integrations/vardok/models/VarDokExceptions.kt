package no.ssb.metadata.vardef.integrations.vardok.models

open class VardokException(
    override val message: String,
) : Exception()

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


package no.ssb.metadata.vardef.integrations.vardok.models

open class VardokException(
    override val message: String,
) : Exception()

class MissingPrimaryLanguageException :
    VardokException(
        "The VarDok definition is missing both Norwegian languages and can not be migrated.",
    )

class VardokNotFoundException(
    message: String,
) : VardokException(message)

class StatisticalUnitException(
    id: String,
) : VardokException(
        "Vardok ID $id: StatisticalUnit is either missing or contains outdated unit types.",
    )

package no.ssb.metadata.vardef.integrations.vardok.models

open class VardokException(
    override val message: String,
) : Exception()

class MissingNbLanguageException :
    VardokException(
        "The VarDok definition is missing the Norwegian Bokmål language and can not be migrated.",
    )

class VardokNotFoundException(
    message: String,
) : VardokException(message)

class StatisticalUnitException(
    id: String,
) : VardokException(
        "Vardok ID $id: StatisticalUnit is either missing or contains outdated unit types.",
    )

class OutdatedSubjectAreaException(
    id: String,
) : VardokException(
        "Vardok id $id SubjectArea has outdated subject area value and can not be saved",
    )

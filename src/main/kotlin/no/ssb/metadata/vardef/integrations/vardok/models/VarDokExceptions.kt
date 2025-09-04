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

class OutdatedSubjectAreaException(
    id: String,
    subjectAreaName: String,
) : VardokException(
        "VarDok variable with ID '$id' SubjectArea '$subjectAreaName' is unknown and could not be " +
            "transformed to a Statistical Subject compatible with Vardef." +
            "Please update the SubjectArea for this variable in VarDok and try again.",
    )

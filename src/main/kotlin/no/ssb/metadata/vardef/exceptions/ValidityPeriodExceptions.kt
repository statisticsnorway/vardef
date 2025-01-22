package no.ssb.metadata.vardef.exceptions

open class ValidityPeriodExceptions(override val message: String) : Exception()

class InvalidValidDateException() : ValidityPeriodExceptions(
    "Invalid 'valid_from' value.",
)

class DefinitionTextUnchangedException() : ValidityPeriodExceptions(
    "Definition text for all languages must be changed when creating a new validity period.",
)

class ClosedValidityPeriodException() : ValidityPeriodExceptions(
    "This validity period is closed, create a new validity period.",
)

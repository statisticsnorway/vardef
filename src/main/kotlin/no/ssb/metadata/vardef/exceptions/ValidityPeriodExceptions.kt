package no.ssb.metadata.vardef.exceptions

open class ValidityPeriodExceptions(override val message: String) : Exception()

class InvalidValidDateException() : ValidityPeriodExceptions(
    "The date selected cannot be added because it falls between previously added valid dates.",
)

class DefinitionTextUnchangedException() : ValidityPeriodExceptions(
    "Definition text for all languages must be changed when creating a new validity period.",
)

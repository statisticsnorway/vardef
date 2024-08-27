package no.ssb.metadata.vardef.exceptions

open class ValidityPeriodException(override val message: String): Exception()

class DefinitionException: ValidityPeriodException("Definition texts is not changed")
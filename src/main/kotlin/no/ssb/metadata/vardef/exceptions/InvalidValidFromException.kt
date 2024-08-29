package no.ssb.metadata.vardef.exceptions

import io.micronaut.core.annotation.Introspected

@Introspected
class InvalidValidFromException(message: String = "Not valid date") : RuntimeException(message)

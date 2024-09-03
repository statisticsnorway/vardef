package no.ssb.metadata.vardef.exceptions

import io.micronaut.core.annotation.Introspected

@Introspected
class InvalidValidFromException(
    message: String = "The date selected cannot be added because it falls between previously added valid from dates.",
) : RuntimeException(message)

package no.ssb.metadata.vardef.exceptions

import io.micronaut.core.annotation.Introspected

@Introspected
class PublishedVariableAccessException(
    message: String = "Only allowed for published variables.",
) : RuntimeException(message)

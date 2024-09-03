package no.ssb.metadata.vardef.exceptions

import io.micronaut.core.annotation.Introspected

@Introspected
class DefinitionTextUnchangedException(
    message: String = "Definition text for all languages must be changed when creating a new validity period.",
) : RuntimeException(message)

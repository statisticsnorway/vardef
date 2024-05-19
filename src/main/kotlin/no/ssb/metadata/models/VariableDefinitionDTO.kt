package no.ssb.metadata.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class VariableDefinitionDTO(var name: Map<SupportedLanguages,String>, var shortName: String?, var definition: Map<SupportedLanguages,String>)

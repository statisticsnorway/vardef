package no.ssb.metadata.vardef.integrations.klass.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ClassificationItems(val codes: List<ClassificationItem>)

package no.ssb.metadata.vardef.integrations.klass.models

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ClassisficationItem(
    val code: String,
    val parentCode: String?,
    val level: Int,
    val name: String,
    val shortName: String,
    val presentationName: String,
    val validFrom: String,
    val validTo: String?,
    val notes: String?
)

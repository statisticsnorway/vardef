package no.ssb.metadata.klass

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class Classifications(val classificationList: List<Any>)

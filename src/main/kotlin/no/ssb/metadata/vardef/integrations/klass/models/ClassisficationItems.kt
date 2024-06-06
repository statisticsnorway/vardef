package no.ssb.metadata.vardef.integrations.klass.models

import ch.qos.logback.core.spi.ErrorCodes
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ClassisficationItems(val codes: List<ClassisficationItem>)

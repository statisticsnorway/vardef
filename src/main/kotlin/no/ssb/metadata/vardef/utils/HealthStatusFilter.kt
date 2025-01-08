package no.ssb.metadata.vardef.utils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.filter.Filter
import ch.qos.logback.core.spi.FilterReply

class HealthStatusFilter : Filter<ILoggingEvent>() {
    override fun decide(event: ILoggingEvent): FilterReply {
        if (event.loggerName == "io.micronaut.management.health.monitor.HealthMonitorTask") {
            return if (event.formattedMessage.contains("status DOWN")) {
                FilterReply.ACCEPT
            } else {
                FilterReply.DENY
            }
        }
        return FilterReply.NEUTRAL
    }
}

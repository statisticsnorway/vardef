package no.ssb.metadata.vardef.utils

import org.slf4j.MDC

inline fun addMDC(
    contextMap: Map<String, String>,
    block: () -> Unit,
) {
    contextMap.forEach { (key, value) -> MDC.put(key, value) }
    try {
        block()
    } finally {
        MDC.clear()
    }
}

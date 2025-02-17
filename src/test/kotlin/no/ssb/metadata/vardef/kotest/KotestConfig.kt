package no.ssb.metadata.vardef.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension

@Suppress("unused")
object KotestConfig : AbstractProjectConfig() {
    override val autoScanEnabled = false
    override fun extensions() = listOf(MicronautKotest5Extension)
}
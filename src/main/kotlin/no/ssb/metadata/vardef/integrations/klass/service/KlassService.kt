package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected

@Prototype
@Introspected
interface KlassService {
    fun getCodesFor(id: String): List<String>
}

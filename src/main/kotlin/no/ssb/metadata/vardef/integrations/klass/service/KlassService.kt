package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse

@Prototype
@Introspected
interface KlassService {
    fun getClassifications(): KlassApiResponse?

    fun getCodesFor(id: String): List<String>
}

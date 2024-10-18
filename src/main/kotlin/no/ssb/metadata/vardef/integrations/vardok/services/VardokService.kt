package no.ssb.metadata.vardef.integrations.vardok.services

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse

@Prototype
@Introspected
interface VardokService {
    fun getVardokItem(id: String): VardokResponse?

    fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse?

    fun fetchMultipleVardokItemsByLanguage(id: String): MutableMap<String, VardokResponse>

}

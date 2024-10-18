package no.ssb.metadata.vardef.integrations.vardok.services

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.vardef.integrations.vardok.models.VardokResponse

@Prototype
@Introspected
interface VarDokService {
    fun getVarDokItem(id: String): VardokResponse?

    fun getVardokByIdAndLanguage(
        id: String,
        language: String,
    ): VardokResponse?
}

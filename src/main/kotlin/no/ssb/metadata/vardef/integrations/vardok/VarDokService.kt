package no.ssb.metadata.vardef.integrations.vardok

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected

@Prototype
@Introspected
interface VarDokService {

    fun getVarDokItem(id: String): VardokResponse?

    fun getVardokByIdAndLanguage(id: String, language: String ): VardokResponse?

}

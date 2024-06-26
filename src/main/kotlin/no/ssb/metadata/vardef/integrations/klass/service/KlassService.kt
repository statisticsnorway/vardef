package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.models.KlassReference
import no.ssb.metadata.models.SupportedLanguages
import no.ssb.metadata.vardef.integrations.klass.models.ClassificationItem

@Prototype
@Introspected
interface KlassService {
    fun getCodesFor(id: String): List<String>

    fun getCodeItemFor(id: String, code: String, language: SupportedLanguages): KlassReference?
}



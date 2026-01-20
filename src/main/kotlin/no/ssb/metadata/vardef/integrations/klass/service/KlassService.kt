package no.ssb.metadata.vardef.integrations.klass.service

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.vardef.models.KlassReference
import no.ssb.metadata.vardef.models.SupportedLanguages

@Prototype
@Introspected
interface KlassService {
    fun getCodesFor(
        id: String,
        level: Int? = null,
    ): List<String>

    fun doesClassificationExist(id: String): Boolean

    fun renderCode(
        classificationId: String,
        code: String,
        language: SupportedLanguages,
    ): KlassReference

    fun getKlassUrlForIdAndLanguage(
        classificationId: String,
        language: SupportedLanguages,
    ): String
}

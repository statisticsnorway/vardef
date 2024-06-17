package no.ssb.metadata.vardef.integrations.klass.service

import no.ssb.metadata.vardef.integrations.klass.models.KlassApiResponse

interface KlassService {
    fun getClassifications(): KlassApiResponse?

    fun getCodesFor(id: String): List<String>
}

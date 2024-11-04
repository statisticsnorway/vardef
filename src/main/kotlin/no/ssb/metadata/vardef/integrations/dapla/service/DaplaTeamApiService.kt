package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected

@Prototype
@Introspected
interface DaplaTeamApiService {
    fun isValidTeam(team: String): Boolean
}

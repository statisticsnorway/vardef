package no.ssb.metadata.vardef.integrations.dapla.service

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.vardef.integrations.dapla.models.Team

@Prototype
@Introspected
interface DaplaTeamService {

    fun getTeam(teamName: String): Team?
    fun isValidTeam(team: String): Boolean
}

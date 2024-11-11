package no.ssb.metadata.vardef.integrations.dapla.service

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team

@Singleton
open class DaplaTeamApiService(private val daplaTeamApiClient: DaplaTeamApiClient) : DaplaTeamService {
    override fun getTeam(teamName: String): Team? {
        TODO("Not yet implemented")
    }

    override fun isValidTeam(team: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isValidGroup(group: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGroup(groupName: String): Group? {
        TODO("Not yet implemented")
    }
}

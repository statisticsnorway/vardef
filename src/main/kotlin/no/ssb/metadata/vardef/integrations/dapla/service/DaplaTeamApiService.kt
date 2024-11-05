package no.ssb.metadata.vardef.integrations.dapla.service

import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team

// @Singleton
open class DaplaTeamApiService(private val daplaTeamApiClient: DaplaTeamApiClient) : DaplaTeamService {
    override fun getTeam(teamName: String): Team? {
        val response = daplaTeamApiClient.fetchTeam(teamName)
        return response.body()
    }

    override fun isValidTeam(team: String): Boolean {
        return getTeam(team) != null
    }

    override fun isValidGroup(group: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGroup(groupName: String): Group? {
        TODO("Not yet implemented")
    }
}

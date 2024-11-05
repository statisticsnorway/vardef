package no.ssb.metadata.vardef.integrations.dapla.service

import jakarta.inject.Singleton
import no.ssb.metadata.vardef.integrations.dapla.models.Team

// @Singleton
open class DaplaTeamApiService(private val daplaTeamApiClient: DaplaTeamApiClient):DaplaTeamService {

    override fun getTeam(teamName: String): Team? {
        val response = daplaTeamApiClient.fetchTeam(teamName)
        return response.body()
    }

    override fun isValidTeam(team: String): Boolean {
        return getTeam(team) != null

    }
}
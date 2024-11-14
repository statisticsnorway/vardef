package no.ssb.metadata.vardef.integrations.dapla.services

import io.micronaut.context.annotation.Prototype
import io.micronaut.core.annotation.Introspected
import no.ssb.metadata.vardef.integrations.dapla.models.Group
import no.ssb.metadata.vardef.integrations.dapla.models.Team

/**
 * Interface representing a service for retrieving and validating teams and groups.
 * This service provides methods to fetch `Team` and `Group` objects by name, and to check
 * whether a given team or group exists.
 *
 * @Prototype Indicates that an instance of the implementing class should be created for each use.
 * @Introspected Marks the interface for introspection, typically used for reflection or metadata processing.
 */
@Prototype
@Introspected
interface DaplaTeamService {
    fun getTeam(teamName: String): Team?

    fun isValidTeam(teamName: String): Boolean

    fun isValidGroup(groupName: String): Boolean

    fun getGroup(groupName: String): Group?
}

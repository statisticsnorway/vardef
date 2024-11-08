package no.ssb.metadata.vardef.services

import no.ssb.metadata.vardef.models.Owner

/**
 * Dapla team service
 *
 * Functionality relating to Dapla teams and their groups.
 */
class DaplaTeamService {
    companion object {
        private const val DEVELOPERS_SUFFIX = "developers"

        /**
         * Is the given group the developers group
         *
         * @param group the group in question
         * @return `true` if it's the developers group
         */
        fun isDevelopers(group: String): Boolean = group.matches(Regex("^.+-developers$"))

        fun containsDevelopersGroup(owner: Owner): Boolean =
            owner.groups.any {
                it == "${owner.team}-$DEVELOPERS_SUFFIX"
            }
    }
}

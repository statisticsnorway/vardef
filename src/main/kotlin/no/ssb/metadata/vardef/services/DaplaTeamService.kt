package no.ssb.metadata.vardef.services

/**
 * Dapla team service
 *
 * Functionality relating to Dapla teams and their groups.
 */
class DaplaTeamService {
    companion object {
        /**
         * Is the given group the developers group
         *
         * @param group the group in question
         * @return `true` if it's the developers group
         */
        fun isDevelopers(group: String): Boolean = group.endsWith("developers")
    }
}
